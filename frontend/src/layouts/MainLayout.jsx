import React from "react";
import { Outlet, useNavigate } from "react-router-dom";

import { Header,
         Footer,
         ScrollToTopButton
       } from "@orif-informatique/react-components-library";
import useAuthStore from "../../authStore";

const MainLayout = () => {
  const navigate = useNavigate();
  const accessToken = useAuthStore((s) => s.accessToken);
  const clearAccessToken = useAuthStore((s) => s.clearAccessToken);

  return (
    <>
      <Header
        title="App title"
        logoPath="/images/logo.svg"
        onLogin={() => navigate('/login')}
        onLogout={() => { clearAccessToken(); navigate('/'); }}
      />
      <main>
        <Outlet />
      </main>
      <ScrollToTopButton onClick={() => {}} />
      <Footer />
    </>
  );
}

export default MainLayout;
