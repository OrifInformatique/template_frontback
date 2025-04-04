import React from "react";
import ScrollToTopButton from "../ui/button/scroll-to-top/ScrollToTopButton";
import Header from "../ui/header/Header";

const MainLayout = () => {
  return (<>
      <Header title="App title" onLogin={() => {}} onLogout={() => {}} />
      <ScrollToTopButton onClick={() => {}} />
  </>);
}

export default MainLayout;