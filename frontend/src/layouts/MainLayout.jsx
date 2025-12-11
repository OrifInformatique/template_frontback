import React from "react";
import { useNavigate} from "react-router-dom";

import { Header,
         Footer,
         ScrollToTopButton
       } from "@orif-informatique/react-components-library";
import useAuthStore from "../../authStore";

const MainLayout = () => {
  const navigate = useNavigate();
  const clearAuth = useAuthStore((s) => s.clearAuth);

  return (
    <>
      <Header
        title="App title"
        logoPath="/images/logo.svg"
        onLogin={() => navigate('/login')}
        onLogout={() => { clearAuth(); navigate('/'); }}
      />
      {/* <Outlet /> */}
      <div className="flex justify-center items-center font-medium text-4xl text-gray-500 h-96 bg-background">
        C O N T E N T
      </div>
      <ScrollToTopButton onClick={() => {}} />
      <Footer />
    </>
  );
}

export default MainLayout;
