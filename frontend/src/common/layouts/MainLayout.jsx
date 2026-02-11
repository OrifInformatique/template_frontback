import React from "react";
import { Outlet, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";

import { Header,
         Footer,
         ScrollToTopButton
       } from "@orif-informatique/react-components-library";
import useAuthStore from "../../features/auth/authStore";

const MainLayout = () => {
  const { t } = useTranslation("common");
  const navigate = useNavigate();
  const clearAuth = useAuthStore((s) => s.clearAuth);


  return (
    <>
      <Header
        title={t("app_title")}
        logoPath="/images/logo.svg"
        onLogin={() => navigate('/login')}
        onLogout={() => { clearAuth(); navigate('/'); }}
      />
      <main className="p-5 sm:p-10 bg-background">
        <Outlet />
      </main>
      <ScrollToTopButton onClick={() => {}} />
      <Footer />
    </>
  );
}

export default MainLayout;
