import TableUser from "./components/TableUser";
import { App } from 'antd';



const Tables = () => {
  const { message, notification } = App.useApp();
  return (
    <div>
      <div className="col-span-1 h-full w-full rounded-xl">
        <TableUser /> {/* Truyền dữ liệu vào đây */}
      </div>
    </div>
  );
};

export default () => (
  <App>
    <Tables />
  </App>
);
