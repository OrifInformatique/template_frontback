import { useCallback, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

import useAuthStore from "../../features/auth/authStore";

const AUTH_STORAGE_KEY = "auth-storage";

const readStoredUser = () => {
  try {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw);
    return parsed?.state?.user || null;
  } catch (e) {
    return null;
  }
};

const useLayoutAuth = () => {
  const navigate = useNavigate();
  const [resetKey, setResetKey] = useState(0);
  const user = useAuthStore((s) => s.user);
  const clearAuth = useAuthStore((s) => s.clearAuth);

  const effectiveUser = useMemo(() => user ?? readStoredUser(), [user]);

  const handleLogout = useCallback(() => {
    clearAuth();
    localStorage.removeItem("loginType");
    navigate("/");
    setResetKey((k) => k + 1); // force remount for a cleaner visual reset
  }, [clearAuth, navigate]);

  return { effectiveUser, resetKey, handleLogout };
};

export default useLayoutAuth;
