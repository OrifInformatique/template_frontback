import React from 'react'
import { Outlet } from "react-router-dom";

const MainLayout = () => {
  return (<>
      <div className="flex w-full h-full p-2 sm:p-8">
        <div className="flex w-full h-full justify-center items-center flex-col border-primary border-4 sm:border-8 p-2 sm:p-8">
          <Outlet />
        </div>
      </div>
  </>)
}

export default MainLayout