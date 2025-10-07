import React, { useEffect, useState } from "react";
import Project from "./components/Project";
import { useAuth } from "hooks/useAuth";
import { App, Button, Tooltip } from 'antd';
import { InfoCircleOutlined } from "@ant-design/icons";
import profileService from "../../../service/profile.service";
import { useCache } from "../../../hooks/useCache";

const ProfileOverview = () => {
  const [tableData, setTableData] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { message, notification } = App.useApp();
  const { token } = useAuth();
  const { clearCache, getCacheStats } = useCache();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const coursesData = await profileService.getCourses();
        setTableData(coursesData.content);
      } catch (error) {
        console.error("Error fetching data:", error);
        setError("Failed to fetch data");
        message.error("Failed to load courses");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [message]);

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>Error: {error}</div>;
  }

  return (
    <div className="flex w-full flex-col gap-5">
      <div className="w-full mt-3 flex h-fit flex-col gap-5 lg:grid lg:grid-cols-12">
      </div>  
      {/* all project & ... */}
      <div className=" h-full  gap-5 lg:!grid-cols-12">
        <div className="col-span-5 lg:col-span-6 lg:mb-0 3xl:col-span-4">
          <Project tableData={tableData} />
        </div>
      </div>
    </div>
  );
};

export default () => (
  <App>
    <ProfileOverview />
  </App>
);
