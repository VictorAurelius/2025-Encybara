import TableUser from "./components/TableUser";
import LearningStatsTable from "./components/LearningStatsTable";
import { App } from 'antd';



const Tables = () => {
  const { message, notification } = App.useApp();
  return (
    <div className="flex flex-col gap-5">
      <div className="col-span-1 h-full w-full rounded-xl">
        <TableUser /> {/* Truyền dữ liệu vào đây */}
      </div>
      <div className="col-span-1 h-full w-full rounded-xl">
        <LearningStatsTable />
      </div>
    </div>
  );
};

export default () => (
  <App>
    <Tables />
  </App>
);
