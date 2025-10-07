import TableTopCreator from "./components/TableCreator";
import { useEffect, useState } from "react";
import { API_BASE_URL } from "service/api.config";
import { App } from 'antd';


const AdminAccount = () => {
  const { message, notification } = App.useApp();
  const [tableData, setTableData] = useState<any[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  const reloadTable = async () => {
    setLoading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/api/v1/admins`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error("Failed to fetch data");
      }

      const data = await response.json();

      setTableData(data.result);
    } catch (error) {
      console.error("Error fetching data:", error);
      setError("Failed to fetch data");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    reloadTable();
  }, []);

  return (
    <div className="col-span-1 h-full w-full rounded-xl">
      <TableTopCreator tableData={tableData} reloadTable={reloadTable} />
      <div className="mb-5" />
    </div>
  );
};

export default () => (
  <App>
    <AdminAccount />
  </App>
);
