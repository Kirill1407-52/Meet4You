import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import App from "./App";
import SearchPage from "./pages/SearchPage";

ReactDOM.createRoot(document.getElementById("root")!).render(
    <BrowserRouter>
        <Routes>
            <Route path="/" element={<App />} />
            <Route path="/search" element={<SearchPage />} />
        </Routes>
    </BrowserRouter>
);
