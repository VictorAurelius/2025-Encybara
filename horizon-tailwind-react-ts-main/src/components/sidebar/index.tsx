/* eslint-disable */

import { HiX } from "react-icons/hi";
import Links from "./components/Links";

import SidebarCard from "components/sidebar/components/SidebarCard";
import routes from "routes";

const Sidebar = (props: {
  open: boolean;
  onClose: React.MouseEventHandler<HTMLSpanElement>;
}) => {
  const { open } = props;
  return (
    <div
      className={`sm:none justify-between duration-175 mb-0 linear fixed !z-50 flex min-h-full flex-col bg-white pb-0 shadow-2xl shadow-white/5 transition-all dark:!bg-navy-800 dark:text-white md:!z-50 lg:!z-50 xl:!z-0 ${open ? "translate-x-0" : "-translate-x-96"
        }`}
    >
      <div className={`mx-[56px] mb-[10px] flex items-center`}>
        <div className="mt-1 ml-1 h-1.5 font-poppins text-[20px] font-bold uppercase text-navy-700 dark:text-white">
          Encybara
        </div>
      </div>
      <div className="mt-[20px] h-px bg-gray-300 dark:bg-white/30" />
      <ul className="mb-auto pt-1">
        <Links routes={routes.filter((route) => !route.hidden)} />
      </ul>
      <div className=" justify-center mb-0">
        <SidebarCard />
      </div>
    </div>
  );
};

export default Sidebar;
