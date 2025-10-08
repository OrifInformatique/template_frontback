import React from "react";
import { Outlet } from "react-router-dom";

import { Header,
         Footer,
         ScrollToTopButton
       } from "@orif-informatique/react-component-library";

const MainLayout = () => {
  return (<>
      <Header title="App title" />
      {/* <Outlet /> */}
      <div className="flex justify-center items-center font-medium text-4xl text-gray-500 h-96 bg-background">
        C O N T E N T
      </div>
      <ScrollToTopButton onClick={() => {}} />
      <Footer />
  </>);
}

export default MainLayout;