import React, { useMemo, useState } from "react";
import { Outlet, useNavigate } from "react-router-dom";

import { Header,
         Footer,
         ScrollToTopButton
       } from "@orif-informatique/react-components-library";
import useAuthStore from "../../features/auth/authStore";

const MainLayout = () => {
  const navigate = useNavigate();
  const [resetKey, setResetKey] = useState(0);
  const user = useAuthStore((s) => s.user);
  const clearAuth = useAuthStore((s) => s.clearAuth);

  const handleLogout = () => {
    clearAuth();
    localStorage.removeItem('loginType');
    navigate('/');
    setResetKey((k) => k + 1); // force remount for a cleaner visual reset
  };

  const storedUser = useMemo(() => {
    try {
      const raw = localStorage.getItem('auth-storage');
      if (!raw) return null;
      const parsed = JSON.parse(raw);
      return parsed?.state?.user || null;
    } catch (e) {
      return null;
    }
  }, []);

  const effectiveUser = user || storedUser;

  const headerChild = useMemo(() => {
    if (!effectiveUser) return null;
    return null;
  }, [effectiveUser?.firstName, effectiveUser?.lastName, effectiveUser?.login]);


  return (
    <>
      <Header
        key={`header-${resetKey}`}
        title="App title"
        logoPath="/images/logo.svg"
        onLogin={() => navigate('/login')}
        onLogout={handleLogout}
        childElement={headerChild}
        user={effectiveUser ? { name: effectiveUser?.firstName || "", role: effectiveUser?.mainRole || "user" } : null}
      />
      <main className="p-5 sm:p-10 bg-background">
        <Outlet key={`outlet-${resetKey}`} />
      </main>
      <ScrollToTopButton onClick={() => {}} />
      <Footer />
    </>
  );
}

export default MainLayout;
