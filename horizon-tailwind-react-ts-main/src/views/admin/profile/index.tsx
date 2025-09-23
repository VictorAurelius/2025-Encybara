import React, { useEffect, useState } from "react";
import Project from "./components/Project";
import { API_BASE_URL } from "service/api.config";
import { useAuth } from "hooks/useAuth";
import { App } from 'antd';

const ProfileOverview = () => {
  const [tableData, setTableData] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { message, notification } = App.useApp();
  const { token } = useAuth();
  useEffect(() => {
    const fetchData = async () => {
      // Lấy token từ local storage

      try {
        const response = await fetch(`${API_BASE_URL}/api/v1/courses`, {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        });

        if (!response.ok) {
          throw new Error("Failed to fetch data");
        }

        const data = await response.json();
        setTableData(data.data.content); // Giả sử dữ liệu trả về là mảng các project
      } catch (error) {
        console.error("Error fetching data:", error);
        setError("Failed to fetch data");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>Error: {error}</div>;
  }

  return (
    <div className="flex w-full flex-col gap-5">
      <div className="w-full mt-3 flex h-fit flex-col gap-5 lg:grid lg:grid-cols-12">
        {/* <div className="z-0 col-span-5 lg:!mb-0">
          <Upload />
        </div> */}
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
