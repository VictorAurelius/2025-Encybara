/* eslint-disable */
import React from "react";
import { Link, useLocation } from "react-router-dom";
import DashIcon from "components/icons/DashIcon";
import Access from "views/admin/access";
// chakra imports

interface RoutesType {
  name: string;
  layout: string;
  path: string;
  icon: JSX.Element;
  component: JSX.Element;
  permission?: {
    module?: string;
    method?: string;
    apiPath?: string;
    resource?: string;
  };
}

export const SidebarLinks = (props: { routes: RoutesType[] }): JSX.Element => {
  // Chakra color mode
  let location = useLocation();

  const { routes } = props;

  // verifies if routeName is the one active (in browser input)
  const activeRoute = (routeName: string) => {
    return location.pathname.includes(routeName);
  };

  const createLinks = (routes: RoutesType[]) => {
    return routes.map((route, index) => {
      if (
        route.layout === "/admin" ||
        route.layout === "/auth" ||
        route.layout === "/rtl"
      ) {
        const linkContent = (
          <Link key={index} to={route.layout + "/" + route.path}>
            <div className="relative mb-3 flex hover:cursor-pointer">
              <li
                className="my-[3px] flex cursor-pointer items-center px-8"
              >
                <span
                  className={`${activeRoute(route.path) === true
                    ? "font-bold text-brand-500 dark:text-white"
                    : "font-medium text-gray-600"
                    }`}
                >
                  {route.icon ? route.icon : <DashIcon />}{" "}
                </span>
                <p
                  className={`leading-1 ml-4 flex ${activeRoute(route.path) === true
                    ? "font-bold text-navy-700 dark:text-white"
                    : "font-medium text-gray-600"
                    }`}
                >
                  {route.name}
                </p>
              </li>
              {activeRoute(route.path) && (
                <div className="absolute right-0 top-px h-9 w-1 rounded-lg bg-brand-500 dark:bg-brand-400" />
              )}
            </div>
          </Link>
        );

        // Nếu route có permission, bọc bằng Access
        if (route.permission) {
          return (
            <Access
              key={index}
              hideChildren={true}
              permission={route.permission}
            >
              {linkContent}
            </Access>
          );
        }

        // Nếu không có permission, hiển thị bình thường
        return linkContent;
      }
      return null;
    });
  };
  // BRAND
  return <>{createLinks(routes)}</>;
};

export default SidebarLinks;
