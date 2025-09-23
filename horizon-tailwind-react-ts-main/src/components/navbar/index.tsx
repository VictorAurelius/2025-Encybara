import React, { useEffect } from "react";
import Dropdown from "components/dropdown";
import { FiAlignJustify } from "react-icons/fi";
import { Link } from "react-router-dom";
import avatarImage from "assets/avatar.png"
import { RiMoonFill, RiSunFill } from "react-icons/ri";
import {
  IoMdNotificationsOutline,
  IoMdInformationCircleOutline,
} from "react-icons/io";
import { CgProfile } from "react-icons/cg"; // Profile icon đẹp
import { useAuth } from "../../hooks/useAuth";
import { useNavigate, useLocation } from 'react-router-dom';
import { Card, Modal } from 'antd';
import { API_BASE_URL } from "service/api.config";

const Navbar = (props: {
  onOpenSidenav: () => void;
  brandText: string;
  secondary?: boolean | string;
}) => {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const { admin } = useAuth();
  const { onOpenSidenav, brandText } = props;
  const [darkmode, setDarkmode] = React.useState(false);
  const [notifications, setNotifications] = React.useState([]);
  const [activeTab, setActiveTab] = React.useState('unread');
  const [user, setUser] = React.useState([]);
  const handleLogout = async () => {
    Modal.confirm({
      title: 'Notification',
      content: 'Are you sure you want to logout?',
      okText: 'Yes',
      cancelText: 'No',
      onOk: async () => {
        try {
          await logout();
          navigate('/auth/sign-in', { replace: true });
        } catch (error) {
          console.error('Logout failed:', error);
        }
      }
    });
  };
  useEffect(() => {
    const fetchNotifications = async () => {
      try {
        const response = await fetch(`${API_BASE_URL}/api/v1/notifications`, {
          headers: {
            'Content-Type': 'application/json',
          }
        });

        const data = await response.json();
        setNotifications(data.data);

        // Fetch user information for each notification
        const userPromises = data.data.map((notification: { userId: number; }) =>
          fetchUserInfor(notification.userId)
        );

        const users = await Promise.all(userPromises);
        setUser(users);

      } catch (error) {
        console.error("Error fetching notifications:", error);
      }
    };

    const fetchUserInfor = async (userid: number) => {
      try {
        const response = await fetch(`${API_BASE_URL}/api/v1/users/${userid}`, {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem("admin_token")}`,
          }
        });

        const userdata = await response.json();
        return userdata;

      } catch (error) {
        console.error("Error fetching user information:", error);
        return null;
      }
    };

    fetchNotifications();

  }, []);
  // Cập nhật hàm markAsRead để xử lý chức năng đánh dấu đã đọc VÀ chuyển hướng
  const markAsRead = async (notificationId: number, targetRoute: string = '/admin/forum') => {
    try {
      // Đánh dấu thông báo đã đọc
      const response = await fetch(`${API_BASE_URL}/api/v1/notifications/read/${notificationId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem("admin_token")}`,
        }
      });

      if (!response.ok) {
        throw new Error('Failed to mark notification as read');
      }

      // Cập nhật state để UI hiển thị đúng
      setNotifications(prev =>
        prev.map(notif =>
          notif.id === notificationId
            ? { ...notif, read: true }
            : notif
        )
      );

      // Chuyển hướng sau khi đánh dấu thành công
      navigate(targetRoute);
    } catch (error) {
      console.error("Error marking notification as read:", error);
    }
  };
  const handleNotificationClick = async (notificationId: number) => {
    navigate('/admin/forum');
  };
  const getUserName = (userId: number) => {
    const userInfo = user.find((u: any) => u.id === userId);
    return userInfo ? userInfo.name : "Unknown User";
  };
  const getUserAvatar = (userId: number) => {
    const userInfo = user.find((u: any) => u.id === userId);
    return userInfo ? userInfo.avatar : "default-avatar.png";
  };

  const isNew = (date: string) => {
    const now = new Date();
    const notificationDate = new Date(date);
    const diffInHours = (now.getTime() - notificationDate.getTime()) / (1000 * 60 * 60);
    return diffInHours <= 24;
  };

  const formatTimeAgo = (date: string) => {
    const now = new Date();
    const notificationDate = new Date(date);
    const diffInMinutes = Math.floor((now.getTime() - notificationDate.getTime()) / (1000 * 60));

    if (diffInMinutes < 60) {
      return `${diffInMinutes} phút`;
    } else if (diffInMinutes < 1440) {
      return `${Math.floor(diffInMinutes / 60)} giờ`;
    } else {
      return `${Math.floor(diffInMinutes / 1440)} ngày`;
    }
  };

  const handleTabChange = (tab: string) => {
    setActiveTab(tab);
  };

  return (
    <nav className="sticky top-4 z-40 flex flex-row flex-wrap items-center justify-between rounded-xl bg-white/10 p-2 backdrop-blur-xl dark:bg-[#0b14374d]">
      <div className="ml-[6px]">
        <div className="h-6 w-[224px] pt-1">
          <a
            className="text-sm font-normal text-navy-700 hover:underline dark:text-white dark:hover:text-white"
            href=" "
          >
            Pages
            <span className="mx-1 text-sm text-navy-700 hover:text-navy-700 dark:text-white">
              {" "}
              /{" "}
            </span>
          </a>
          <Link
            className="text-sm font-normal capitalize text-navy-700 hover:underline dark:text-white dark:hover:text-white"
            to="#"
          >
            {brandText}
          </Link>
        </div>
        <p className="shrink text-[33px] capitalize text-navy-700 dark:text-white">
          <Link
            to="#"
            className="font-bold capitalize hover:text-navy-700 dark:hover:text-white"
          >
            {brandText}
          </Link>
        </p>
      </div>

      <div className="relative mt-[3px] flex h-[61px] w-[100px] flex-grow items-center justify-around gap-2 rounded-full bg-white px-2 py-2 shadow-xl shadow-shadow-500 dark:!bg-navy-800 dark:shadow-none md:w-[365px] md:flex-grow-0 md:gap-1 xl:w-[365px] xl:gap-2">

        <span
          className="flex cursor-pointer text-xl text-gray-600 dark:text-white xl:hidden"
          onClick={onOpenSidenav}
        >
          <FiAlignJustify className="h-5 w-5" />
        </span>
        <Dropdown
          button={
            <p className="cursor-pointer">
              <IoMdNotificationsOutline className="h-4 w-4 text-gray-600 dark:text-white" />
            </p>
          }
          children={
            <div className="flex w-[350px] flex-col bg-white rounded-[20px] shadow-xl dark:!bg-navy-700 dark:text-white">
              {/* Header */}
              <div className="flex justify-between items-center p-4 border-b">
                <h3 className="font-semibold text-lg">Notifications</h3>
                <div className="flex gap-2">
                  <button
                    className={`px-3 py-1 text-sm rounded-full ${activeTab === 'read' ? 'bg-blue-50 text-blue-600' : 'text-gray-600'}`}
                    onClick={() => handleTabChange('read')}
                  >
                    Read
                  </button>
                  <button
                    className={`px-3 py-1 text-sm rounded-full ${activeTab === 'unread' ? 'bg-blue-50 text-blue-600' : 'text-gray-600'}`}
                    onClick={() => handleTabChange('unread')}
                  >
                    Unread
                  </button>
                </div>
              </div>

              {/* Notifications List */}
              <div className="flex flex-col max-h-[400px] overflow-y-auto">
                {activeTab === 'unread' && (
                  <div className="p-2">
                    {notifications.filter(n => !n.read).map((notification) => (
                      <div
                        key={notification.id}
                        className="flex items-start gap-3 p-2 hover:bg-gray-50 cursor-pointer"
                        onClick={() => markAsRead(notification.id, '/admin/forum')}  // Truyền route đích
                      >
                        <div className="relative">
                          <img
                            src={getUserAvatar(notification.userId)}
                            className="w-10 h-10 rounded-full"
                            alt="User avatar"
                          />
                          {notification.isLive && (
                            <div className="absolute -top-1 -right-1">
                              <div className="flex items-center justify-center w-5 h-5 bg-red-500 rounded-full">
                                <span className="text-[10px] text-white">LIVE</span>
                              </div>
                            </div>
                          )}
                        </div>
                        <div className="flex-1">
                          <p className="text-sm font-bold">{getUserName(notification.userId)}</p>
                          <p className="text-sm">{notification.message}</p>
                          <p className="text-xs text-gray-500 mt-1">{formatTimeAgo(notification.createdAt)}</p>
                        </div>
                        <div className="w-2 h-2 bg-blue-600 rounded-full mt-2"></div>
                      </div>
                    ))}
                  </div>
                )}

                {activeTab === 'read' && (
                  <div className="p-2">
                    {notifications.filter(n => n.read).map((notification) => (
                      <div
                        key={notification.id}
                        className="flex items-start gap-3 p-2 hover:bg-gray-50 cursor-pointer"
                        onClick={() => navigate('/admin/forum')}  // Chỉ cần navigate vì đã đọc rồi
                      >
                        <img
                          src={getUserAvatar(notification.userId)}
                          className="w-10 h-10 rounded-full"
                          alt="User avatar"
                        />
                        <div className="flex-1">
                          <p className="text-sm font-bold">{getUserName(notification.userId)}</p>
                          <p className="text-sm">{notification.message}</p>
                          <p className="text-xs text-gray-500 mt-1">{formatTimeAgo(notification.createdAt)}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>

            </div>
          }
          classNames={"py-2 top-6 -left-[250px] md:-left-[330px] w-max"}
          animation="origin-[75%_0%] md:origin-top-right transition-all duration-300 ease-in-out"
        />
        <Dropdown
          button={
            <p className="cursor-pointer">
              <IoMdInformationCircleOutline className="h-4 w-4 text-gray-600 dark:text-white" />
            </p>
          }
          children={
            <div className="flex w-[280px] flex-col gap-2 rounded-[20px] bg-white p-4 shadow-xl shadow-shadow-500 dark:!bg-navy-700 dark:text-white dark:shadow-none">
              <div
                style={{
                  backgroundImage: `url(${avatarImage})`,
                  backgroundRepeat: "no-repeat",
                  backgroundSize: "cover",
                  height: "200px",
                  width: "100%",
                }}
                className=" w-full aspect-video w-full rounded-lg "
              />

              <a
                target="blank"
                href="https://horizon-ui.com/docs-tailwind/docs/react/installation?ref=live-free-tailwind-react"
                className="px-full linear flex cursor-pointer items-center justify-center rounded-xl border py-[11px] font-bold text-navy-700 transition duration-200 hover:bg-gray-200 hover:text-navy-700 dark:!border-white/10 dark:text-white dark:hover:bg-white/20 dark:hover:text-white dark:active:bg-white/10"
              >
                Nhóm NCKH IT1
              </a>

            </div>
          }
          classNames={"py-2 top-6 -left-[130px] md:-left-[130px] w-max"}
          animation="origin-[75%_0%] md:origin-top-mid transition-all duration-300 ease-in-out"
        />

        {/* Profile & Dropdown */}
        <Dropdown
          button={
            <CgProfile
              className="h-7 w-7 text-gray-600 hover:text-gray-800 cursor-pointer"
            />
          }
          children={
            <div className="flex h-25 w-56 flex-col justify-start rounded-[20px] bg-white bg-cover bg-no-repeat shadow-xl shadow-shadow-500 dark:!bg-navy-700 dark:text-white dark:shadow-none">
              <div className="mt-3 ml-4">
                <div className="flex items-center gap-2">
                  <p className="text-sm font-bold text-navy-700 dark:text-white">
                    👋 Hey, {admin.name}
                  </p>{" "}
                </div>
              </div>
              <div className="mt-3 h-px w-full bg-gray-200 dark:bg-white/20 " />

              <div className="mt-2 ml-4 flex flex-col">
                {/* <Link
                  to="/admin/profile"
                  className="text-sm text-gray-800 dark:text-white hover:dark:text-white"
                >
                  Thông tin cá nhân
                </Link> */}
                <button
                  onClick={handleLogout}
                  className="mt-0 mb-1 text-sm font-medium text-red-500 hover:text-red-600 text-left"
                >

                  Logout                </button>
              </div>
            </div>
          }
          classNames={"py-2 top-8 -left-[180px] w-max"}
        />
      </div>
    </nav>
  );
};

export default Navbar;
