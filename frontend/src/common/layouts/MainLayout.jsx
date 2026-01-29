import React from "react";
import { Outlet, useNavigate } from "react-router-dom";

import { Header,
         Footer,
         ScrollToTopButton
       } from "@orif-informatique/react-components-library";
import useLayoutAuth from "../hooks/useLayoutAuth";

const MainLayout = () => {
  const navigate = useNavigate();
  const { effectiveUser, remountKey, handleLogout } = useLayoutAuth();


  return (
    <>
      <Header
        key={`header-${remountKey}`}
        title="App title"
        logoPath="/images/logo.svg"
        onLogin={() => navigate('/login')}
        onLogout={handleLogout}
        user={effectiveUser ? { name: effectiveUser?.firstName || "", role: effectiveUser?.mainRole || "user" } : null}
      />
      <main className="p-5 sm:p-10 bg-background">
        <Outlet key={`outlet-${remountKey}`} />
      </main>
      <ScrollToTopButton onClick={() => {}} />
      <Footer />
    </>
  );
}

export default MainLayout;
