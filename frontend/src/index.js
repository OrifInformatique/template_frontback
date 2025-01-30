import React from 'react';
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Routes, Route } from "react-router-dom";

// Layouts
import MainLayout from './layouts/main';

// Modules
import Home from './modules/home';
import Contact from './modules/contact';

// Utils
import Redirect from './utils/Redirect'

// Styles
import './index.pcss';

const container = document.getElementById('root');
const root = createRoot(container);


root.render(<BrowserRouter basename={process.env.APP_ROOT}>

    <Routes>
      <Route path="/" element={<MainLayout />}>
        <Route index element={<Home />} />
        <Route path="contact" element={<Contact />} />
        <Route path="*" element={<Redirect to="/" />} />
      </Route>

    </Routes>

</BrowserRouter>);