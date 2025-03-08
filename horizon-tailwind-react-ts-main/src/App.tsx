import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import routes from "./routes";
import ProtectedRoute from "./components/ProtectedRoute";
import AdminLayout from "layouts/admin";
import AuthLayout from "layouts/auth";
import UserProfilePage from "views/admin/user";

const App = () => {
  return (

    <Routes>
      {/* Auth Routes */}
      <Route path="auth/*" element={<AuthLayout />}>
        {routes
          .filter(route => route.layout === "/auth")
          .map(({ path, component }) => (
            <Route key={path} path={path} element={component} />
          ))}


      </Route>

      {/* Admin Routes */}
      <Route
        path="admin/*"
        element={
          <ProtectedRoute>
            <AdminLayout />
          </ProtectedRoute>
        }
      >
        {routes
          .filter(route => route.layout === "/admin")
          .map(({ path, component }) => (
            <Route key={path} path={path} element={component} />
          ))}
        <Route key={"profile/:userId"} path={"profile/:userId"} element={<UserProfilePage />} />
      </Route>

      {/* Default Route */}
      <Route path="/" element={<Navigate to="/admin/default" replace />} />
      <Route path="*" element={<Navigate to="/admin/default" replace />} />
    </Routes>

  );
};

export default App;
