import { useState, useEffect } from 'react';
import { 
  signInWithPopup, 
  GoogleAuthProvider, 
  onAuthStateChanged,
  User
} from 'firebase/auth';
import { doc, getDoc, setDoc } from 'firebase/firestore';
import { auth, db, handleFirestoreError, OperationType } from './lib/firebase';
import AdminApp from './components/AdminApp';
import DriverApp from './components/DriverApp';
import AndroidInstallBanner from './components/AndroidInstallBanner';
import { 
  Droplet, 
  LayoutDashboard,
  Lock,
  ChevronRight,
  Shield,
  AlertCircle,
  Truck,
  ArrowLeft,
  Smartphone
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';

export default function App() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  // Specific role persistence: if they entered as driver, stays driver; if admin, stays admin
  const [selectedAccess, setSelectedAccess] = useState<'admin' | 'driver' | null>(() => {
    return (localStorage.getItem('pipa_access_role') as 'admin' | 'driver') || null;
  });

  const [appRole, setAppRole] = useState<'admin' | 'driver' | null>(() => {
    const role = (localStorage.getItem('pipa_access_role') as 'admin' | 'driver') || null;
    const adminAuth = localStorage.getItem('pipa_admin_auth') === 'true';
    if (role === 'driver') return 'driver';
    if (role === 'admin' && adminAuth) return 'admin';
    return null;
  });
  
  // Admin Password States
  const [adminPassword, setAdminPassword] = useState('');
  const [storedAdminPassword, setStoredAdminPassword] = useState<string | null>(null);
  const [isAdminAuthenticated, setIsAdminAuthenticated] = useState<boolean>(() => {
    return localStorage.getItem('pipa_admin_auth') === 'true';
  });
  const [isCheckingPassword, setIsCheckingPassword] = useState(false);
  const [passwordError, setPasswordError] = useState('');
  const [showPasswordPrompt, setShowPasswordPrompt] = useState(false);
  const [isLoggingInGoogle, setIsLoggingInGoogle] = useState(false);

  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [firebaseError, setFirebaseError] = useState<string | null>(null);
  const [quotaExceeded, setQuotaExceeded] = useState(false);

  useEffect(() => {
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);
    const handleQuotaExceeded = () => setQuotaExceeded(true);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);
    window.addEventListener('firebase-quota-exceeded', handleQuotaExceeded);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
      window.removeEventListener('firebase-quota-exceeded', handleQuotaExceeded);
    };
  }, []);

  useEffect(() => {
    return onAuthStateChanged(auth, (u) => {
      setUser(u);
      if (!u) {
        // Logged out: reset roles and clear persistence
        setAppRole(null);
        setSelectedAccess(null);
        setIsAdminAuthenticated(false);
        setShowPasswordPrompt(false);
        localStorage.removeItem('pipa_access_role');
        localStorage.removeItem('pipa_admin_auth');
      } else {
        const savedRole = localStorage.getItem('pipa_access_role') as 'admin' | 'driver' | null;
        const savedAdminAuth = localStorage.getItem('pipa_admin_auth') === 'true';
        if (savedRole === 'driver') {
          setAppRole('driver');
          setSelectedAccess('driver');
        } else if (savedRole === 'admin') {
          setSelectedAccess('admin');
          if (savedAdminAuth) {
            setAppRole('admin');
            setIsAdminAuthenticated(true);
          }
        }
      }
      setLoading(false);
    });
  }, []);

  const fetchAdminConfig = async () => {
    setFirebaseError(null);
    try {
      const adminDoc = await getDoc(doc(db, 'config', 'admin'));
      if (adminDoc.exists()) {
        const currentPass = adminDoc.data()?.password;
        if (currentPass === '1234' || !currentPass) {
          try {
            await setDoc(doc(db, 'config', 'admin'), {
              password: '123456',
              updatedAt: new Date().toISOString()
            }, { merge: true });
          } catch (e) {
            console.warn("Could not update doc:", e);
          }
          setStoredAdminPassword('123456');
        } else {
          setStoredAdminPassword(currentPass);
        }
      } else {
        try {
          await setDoc(doc(db, 'config', 'admin'), {
            password: '123456',
            updatedAt: new Date().toISOString()
          }, { merge: true });
        } catch (e) {
          console.warn("Could not create admin doc:", e);
        }
        setStoredAdminPassword('123456');
      }
    } catch (error) {
      console.error("Error fetching admin config:", error);
      const errorMessage = error instanceof Error ? error.message : String(error);
      
      if (errorMessage.includes('offline') || (error as any)?.code === 'unavailable' || errorMessage.includes('unavailable')) {
        setFirebaseError("Sistema operando em modo offline. Algumas funções podem estar limitadas.");
      } else if (errorMessage.includes('permission')) {
        handleFirestoreError(error, OperationType.GET, 'config/admin');
      } else {
        setFirebaseError("Erro ao conectar com o servidor. Usando configurações padrão.");
      }
      
      setStoredAdminPassword('123456');
    }
  };

  const handleChooseAccess = async (choice: 'admin' | 'driver') => {
    setSelectedAccess(choice);
    if (choice === 'driver') {
      if (user) {
        localStorage.setItem('pipa_access_role', 'driver');
        setAppRole('driver');
      }
      // If !user, the Google login screen will be rendered specifically for Motorista
    } else if (choice === 'admin') {
      if (user) {
        // Already logged into Google, check password
        setIsCheckingPassword(true);
        await fetchAdminConfig();
        setIsCheckingPassword(false);
        setShowPasswordPrompt(true);
      }
      // If !user, the Google login screen will be rendered specifically for Administração
    }
  };

  const loginWithGoogle = async (targetRole?: 'admin' | 'driver') => {
    const role = targetRole || selectedAccess;
    setIsLoggingInGoogle(true);
    try {
      const provider = new GoogleAuthProvider();
      const result = await signInWithPopup(auth, provider);
      if (result.user) {
        if (role === 'driver') {
          localStorage.setItem('pipa_access_role', 'driver');
          setAppRole('driver');
          setSelectedAccess('driver');
        } else if (role === 'admin') {
          setSelectedAccess('admin');
          setIsCheckingPassword(true);
          await fetchAdminConfig();
          setIsCheckingPassword(false);
          setShowPasswordPrompt(true);
        }
      }
    } catch (error) {
      console.error("Erro ao autenticar com Google:", error);
    } finally {
      setIsLoggingInGoogle(false);
    }
  };

  const handleResetToDefaultPassword = async () => {
    try {
      await setDoc(doc(db, 'config', 'admin'), {
        password: '123456',
        updatedAt: new Date().toISOString()
      }, { merge: true });
    } catch (e) {
      console.warn("Could not save to firestore:", e);
    }
    setStoredAdminPassword('123456');
    setAdminPassword('123456');
    setPasswordError('');
  };

  const verifyPassword = async () => {
    const effectivePassword = storedAdminPassword || '123456';

    if (adminPassword === '123456' || adminPassword === effectivePassword) {
      if (effectivePassword !== '123456' && adminPassword === '123456') {
        try {
          await setDoc(doc(db, 'config', 'admin'), {
            password: '123456',
            updatedAt: new Date().toISOString()
          }, { merge: true });
          setStoredAdminPassword('123456');
        } catch (e) {
          console.warn("Could not update admin password in firestore:", e);
        }
      }
      localStorage.setItem('pipa_access_role', 'admin');
      localStorage.setItem('pipa_admin_auth', 'true');
      setIsAdminAuthenticated(true);
      setAppRole('admin');
      setShowPasswordPrompt(false);
      setPasswordError('');
    } else {
      setPasswordError('Senha incorreta');
    }
  };

  if (loading) {
    return (
      <div className="h-screen flex items-center justify-center bg-brand-primary/10">
        <motion.div animate={{ rotate: 360 }} transition={{ repeat: Infinity, duration: 1 }} className="text-brand-dark">
          <Droplet size={48} />
        </motion.div>
      </div>
    );
  }

  // Active View: Motorista (Locked role, no role switching)
  if (user && appRole === 'driver') {
    return (
      <div className="h-screen flex flex-col relative">
        {quotaExceeded && (
          <div className="bg-amber-500 text-amber-950 font-semibold text-xs sm:text-sm py-3 px-4 text-center justify-center flex flex-col sm:flex-row items-center gap-2 select-none shadow-md z-[1000] border-b border-amber-600/30">
            <div className="flex items-center gap-1.5">
              <AlertCircle size={16} className="shrink-0 stroke-[2.5]" />
              <strong className="uppercase tracking-wide text-[10px] bg-amber-600/30 px-1.5 py-0.5 rounded">Cota do Firebase Excedida</strong>
            </div>
            <span>Operando de forma estável no <strong>Modo de Contingência Local</strong>. Suas ações e entregas serão salvas no aparelho!</span>
          </div>
        )}
        <DriverApp />
      </div>
    );
  }

  // Active View: Administração (Locked role, no role switching)
  if (user && appRole === 'admin' && isAdminAuthenticated) {
    return (
      <div className="h-screen flex flex-col relative">
        {quotaExceeded && (
          <div className="bg-amber-500 text-amber-950 font-semibold text-xs sm:text-sm py-3 px-4 text-center justify-center flex flex-col sm:flex-row items-center gap-2 select-none shadow-md z-[1000] border-b border-amber-600/30">
            <div className="flex items-center gap-1.5">
              <AlertCircle size={16} className="shrink-0 stroke-[2.5]" />
              <strong className="uppercase tracking-wide text-[10px] bg-amber-600/30 px-1.5 py-0.5 rounded">Cota do Firebase Excedida</strong>
            </div>
            <span>Operando de forma estável no <strong>Modo de Contingência Local</strong>. Suas ações e entregas serão salvas no aparelho!</span>
          </div>
        )}
        <AdminApp />
      </div>
    );
  }

  // Flow Step 2: User selected an access role first, and now the Google Login appears for that specific access
  if (selectedAccess && !user) {
    const isAdmin = selectedAccess === 'admin';
    return (
      <div className="min-h-[100dvh] flex items-center justify-center bg-brand-primary p-3.5 sm:p-6 overflow-y-auto">
        <motion.div 
          initial={{ scale: 0.95, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          className="max-w-md w-full bg-white p-6 sm:p-10 rounded-[32px] sm:rounded-[44px] shadow-2xl border-4 border-brand-dark text-center relative overflow-hidden my-auto"
        >
          {/* Top accent line */}
          <div className={`absolute top-0 left-0 w-full h-2 ${isAdmin ? 'bg-brand-dark' : 'bg-brand-teal'}`} />

          {/* Role badge */}
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-wider mb-6 bg-slate-100 text-slate-700 border border-slate-200">
            {isAdmin ? <Shield size={14} className="text-brand-dark" /> : <Truck size={14} className="text-brand-teal" />}
            <span>Acesso Selecionado: {isAdmin ? 'Administração' : 'Motorista'}</span>
          </div>

          <div className={`w-20 h-20 sm:w-24 sm:h-24 ${isAdmin ? 'bg-brand-dark' : 'bg-brand-teal'} rounded-2xl sm:rounded-[28px] flex items-center justify-center text-white mx-auto mb-5 shadow-xl relative overflow-hidden`}>
            {isAdmin ? (
              <LayoutDashboard size={36} className="stroke-[2]" />
            ) : (
              <Truck size={36} className="stroke-[2]" />
            )}
          </div>

          <h2 className="text-xl sm:text-2xl font-black mb-1.5 tracking-tight text-brand-dark uppercase">
            {isAdmin ? 'Login da Administração' : 'Login do Motorista'}
          </h2>
          <p className="text-slate-500 mb-8 text-xs sm:text-sm leading-relaxed px-2">
            {isAdmin 
              ? 'Conecte sua conta Google para gerenciar frotas, entregas e relatórios.' 
              : 'Conecte sua conta Google para acessar rotas e registrar entregas de água.'}
          </p>

          <button 
            disabled={isLoggingInGoogle}
            onClick={() => loginWithGoogle(selectedAccess)}
            className="w-full bg-white text-slate-800 border-2 border-slate-200 font-extrabold py-3.5 sm:py-4 px-4 rounded-2xl sm:rounded-3xl flex items-center justify-center gap-3 hover:bg-slate-50 hover:border-slate-300 transition-all active:scale-95 shadow-lg shadow-slate-100 uppercase tracking-wider text-xs sm:text-sm disabled:opacity-50"
          >
            {isLoggingInGoogle ? (
              <Droplet className="animate-spin text-brand-dark" size={20} />
            ) : (
              <svg viewBox="0 0 24 24" width="20" height="20" xmlns="http://www.w3.org/2000/svg" className="shrink-0">
                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" fill="#FBBC05"/>
                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z" fill="#EA4335"/>
              </svg>
            )}
            Entrar com Google
          </button>

          <div className="mt-6 pt-5 border-t border-slate-100">
            <button
              onClick={() => {
                setSelectedAccess(null);
                setShowPasswordPrompt(false);
              }}
              className="text-slate-400 hover:text-slate-700 font-bold text-xs uppercase tracking-wider inline-flex items-center gap-1.5 transition-colors"
            >
              <ArrowLeft size={14} /> Escolher outro tipo de acesso
            </button>
          </div>
        </motion.div>
      </div>
    );
  }

  // Flow Step 1: User chooses access option FIRST (Administração ou Motorista)
  return (
    <div className="min-h-[100dvh] flex items-center justify-center bg-brand-primary p-3.5 sm:p-6 overflow-y-auto">
      <div className="max-w-2xl w-full my-auto py-4">
        
        {/* Header Branding */}
        <div className="text-center mb-6 sm:mb-10">
          <div className="w-16 h-16 sm:w-20 sm:h-20 bg-white rounded-2xl sm:rounded-3xl flex items-center justify-center mx-auto mb-3 shadow-xl p-2 border-2 border-brand-dark/10">
            <img 
              src="https://i.ibb.co/sdCcYPpy/logo-inhapi-NNPe-Z.webp" 
              alt="Brasão de Inhapi" 
              className="w-full h-full object-contain"
              onError={(e) => { e.currentTarget.src = 'https://placehold.co/100x100?text=INHAPI'; }}
            />
          </div>
          <h1 className="text-2xl sm:text-3xl font-black tracking-tight text-brand-dark uppercase">
            Operação Pipa
          </h1>
          <p className="font-bold text-brand-teal uppercase tracking-widest text-[10px] sm:text-xs mt-0.5">
            Gestão Hídrica de Inhapi • AL
          </p>
          <div className="inline-block mt-3 px-4 py-1.5 bg-white/70 backdrop-blur-sm rounded-full border border-brand-dark/10 shadow-sm">
            <p className="text-[11px] font-black uppercase tracking-wider text-slate-800">
              Selecione seu tipo de acesso para continuar:
            </p>
          </div>
        </div>

        {/* Role Choice Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 sm:gap-6">
          {/* Opção 1: Administração */}
          <button 
            disabled={isCheckingPassword}
            onClick={() => handleChooseAccess('admin')}
            className="group bg-white p-6 sm:p-8 rounded-3xl sm:rounded-[40px] shadow-2xl border-4 border-brand-dark text-left hover:bg-brand-dark hover:text-white transition-all hover:scale-[1.02] disabled:opacity-50 active:scale-95 flex flex-col justify-between"
          >
            <div>
              <div className="w-14 h-14 sm:w-16 sm:h-16 bg-brand-dark text-white rounded-2xl sm:rounded-[24px] flex items-center justify-center mb-4 group-hover:bg-white group-hover:text-brand-dark transition-colors shadow-lg">
                <LayoutDashboard size={28} className="stroke-[2]" />
              </div>
              <div className="inline-block px-2.5 py-0.5 rounded-full bg-slate-100 group-hover:bg-white/20 text-brand-dark group-hover:text-white text-[9px] font-black uppercase tracking-wider mb-2">
                Gestão & Frota
              </div>
              <h2 className="text-xl sm:text-2xl font-black mb-1.5 uppercase tracking-tight">
                Administração
              </h2>
              <p className="text-slate-500 text-xs sm:text-sm group-hover:text-white/85 leading-relaxed">
                Monitoramento em tempo real, relatórios, cadastros e controle geral da operação.
              </p>
            </div>

            <div className="mt-6 pt-4 border-t border-slate-100 group-hover:border-white/15 flex items-center justify-between text-xs font-black uppercase tracking-wider text-brand-dark group-hover:text-white">
              <span>Entrar como Admin</span>
              <ChevronRight size={18} className="group-hover:translate-x-1 transition-transform" />
            </div>
          </button>

          {/* Opção 2: Motorista */}
          <button 
            onClick={() => handleChooseAccess('driver')}
            className="group bg-white p-6 sm:p-8 rounded-3xl sm:rounded-[40px] shadow-2xl border-4 border-brand-teal text-left hover:bg-brand-teal hover:text-white transition-all hover:scale-[1.02] active:scale-95 flex flex-col justify-between"
          >
            <div>
              <div className="w-14 h-14 sm:w-16 sm:h-16 bg-brand-teal text-white rounded-2xl sm:rounded-[24px] flex items-center justify-center mb-4 group-hover:bg-white group-hover:text-brand-teal transition-colors shadow-lg">
                <Truck size={28} className="stroke-[2]" />
              </div>
              <div className="inline-block px-2.5 py-0.5 rounded-full bg-cyan-50 group-hover:bg-white/20 text-brand-teal group-hover:text-white text-[9px] font-black uppercase tracking-wider mb-2">
                Campo & Entregas
              </div>
              <h2 className="text-xl sm:text-2xl font-black mb-1.5 uppercase tracking-tight text-brand-teal group-hover:text-white">
                Motorista
              </h2>
              <p className="text-slate-500 text-xs sm:text-sm group-hover:text-white/85 leading-relaxed">
                Visualização de rotas, navegação GPS, registro e confirmação fotográfica das entregas.
              </p>
            </div>

            <div className="mt-6 pt-4 border-t border-slate-100 group-hover:border-white/15 flex items-center justify-between text-xs font-black uppercase tracking-wider text-brand-teal group-hover:text-white">
              <span>Entrar como Motorista</span>
              <ChevronRight size={18} className="group-hover:translate-x-1 transition-transform" />
            </div>
          </button>
        </div>

        {/* Android App Quick Install Card */}
        <div className="mt-5 bg-white/95 backdrop-blur-md p-4 sm:p-5 rounded-3xl border-2 border-emerald-500/40 shadow-xl flex flex-col sm:flex-row items-center justify-between gap-4 text-center sm:text-left">
          <div className="flex items-center gap-3.5">
            <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-emerald-500 to-teal-600 text-white flex items-center justify-center shrink-0 shadow-md">
              <Smartphone size={24} />
            </div>
            <div>
              <div className="flex items-center justify-center sm:justify-start gap-2">
                <span className="text-[10px] font-black uppercase tracking-wider bg-emerald-100 text-emerald-800 px-2 py-0.5 rounded-md border border-emerald-200">
                  Aplicativo Android
                </span>
                <span className="text-[10px] font-bold text-slate-500">Instalação Direta</span>
              </div>
              <h3 className="font-black text-slate-900 text-sm mt-0.5">
                Usando no Celular ou Tablet?
              </h3>
              <p className="text-xs text-slate-500 max-w-md">
                Instale a versão Android com 1 toque para abrir em tela cheia e usar rotas offline no caminhão.
              </p>
            </div>
          </div>
          <AndroidInstallBanner variant="button" className="w-full sm:w-auto justify-center shrink-0" />
        </div>

        {/* Footer Info & Online Status */}
        <div className="text-center mt-6 sm:mt-8 space-y-2">
          {!isOnline && (
            <div className="bg-amber-500/10 text-amber-600 px-3.5 py-1.5 rounded-full text-[10px] font-black uppercase tracking-widest border border-amber-500/20 inline-flex items-center gap-2">
              <AlertCircle size={14} /> Modo Offline Ativo
            </div>
          )}
          {firebaseError && (
            <div className="bg-red-500/10 text-red-600 px-4 py-2 rounded-xl text-[10px] font-bold uppercase tracking-wider border border-red-500/20 max-w-sm mx-auto">
              {firebaseError}
            </div>
          )}
          {user && (
            <div className="pt-2 flex items-center justify-center gap-2 text-slate-500 text-xs">
              <span>Conectado como: <strong>{user.email}</strong></span>
              <span>•</span>
              <button 
                onClick={() => auth.signOut()} 
                className="text-red-600 hover:underline font-bold uppercase text-[11px] tracking-wider"
              >
                Sair da conta
              </button>
            </div>
          )}
        </div>

      </div>

      {/* Password Prompt Modal for Admin */}
      <AnimatePresence>
        {showPasswordPrompt && (
          <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-[500] flex items-center justify-center p-4">
            <motion.div 
              initial={{ scale: 0.9, opacity: 0, y: 20 }}
              animate={{ scale: 1, opacity: 1, y: 0 }}
              exit={{ scale: 0.9, opacity: 0, y: 20 }}
              className="bg-white p-5 sm:p-8 rounded-3xl sm:rounded-[40px] w-full max-w-sm shadow-2xl border border-slate-100 max-h-[92dvh] overflow-y-auto"
            >
              <div className="w-16 h-16 bg-brand-dark rounded-[24px] flex items-center justify-center text-white mx-auto mb-5 shadow-xl shadow-brand-dark/20">
                <Lock size={30} />
              </div>
              <h3 className="text-xl font-black text-center mb-1 text-slate-900 uppercase tracking-tight">
                Acesso Administrativo
              </h3>
              <p className="text-slate-500 text-xs sm:text-sm text-center mb-5 leading-relaxed">
                Digite a senha de administrador para continuar. <br/>
                <span className="text-[11px] text-blue-700 bg-blue-50 border border-blue-200/60 font-black px-3 py-1 rounded-full inline-block mt-2">
                  Senha Padrão: 123456
                </span>
              </p>

              <div className="space-y-4">
                <div className="relative">
                  <input 
                    type="password"
                    value={adminPassword}
                    onChange={(e) => {
                      setAdminPassword(e.target.value);
                      setPasswordError('');
                    }}
                    placeholder="Digite a senha (123456)"
                    className={`w-full bg-slate-50 border-2 ${passwordError ? 'border-red-400' : 'border-slate-200'} focus:border-brand-dark outline-none rounded-2xl py-3.5 px-5 font-bold transition-all placeholder:font-normal text-sm`}
                    onKeyDown={(e) => e.key === 'Enter' && verifyPassword()}
                    autoFocus
                  />
                  {passwordError && (
                    <motion.div 
                      initial={{ opacity: 0, y: -10 }}
                      animate={{ opacity: 1, y: 0 }}
                      className="flex items-center gap-1.5 text-red-500 text-xs font-bold mt-2 ml-2"
                    >
                      <AlertCircle size={14} /> {passwordError}
                    </motion.div>
                  )}
                </div>

                <div className="flex items-center justify-between px-1">
                  <button
                    type="button"
                    onClick={handleResetToDefaultPassword}
                    className="text-[11px] text-brand-teal hover:text-brand-dark font-bold transition-colors underline"
                  >
                    Preencher 123456
                  </button>
                  <span className="text-[10px] text-slate-400 font-mono uppercase tracking-wider">Inhapi/AL</span>
                </div>

                <button 
                  onClick={verifyPassword}
                  className="w-full bg-brand-dark text-white font-black py-3.5 rounded-2xl flex items-center justify-center gap-2 hover:bg-slate-800 transition-all active:scale-95 shadow-xl shadow-brand-dark/20 uppercase tracking-widest text-xs"
                >
                  <span>Acessar Painel</span>
                  <ChevronRight size={18} />
                </button>

                <button 
                  onClick={() => {
                    setShowPasswordPrompt(false);
                    setSelectedAccess(null);
                    setAdminPassword('');
                    setPasswordError('');
                  }}
                  className="w-full text-slate-400 hover:text-slate-600 font-bold py-2 text-xs uppercase tracking-wider transition-colors"
                >
                  Voltar
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
}
