import React, { useRef, useState } from "react";
import Card from "components/card";
import { Badge, message, notification } from "antd";
import ModalAdmin, { IAdmin } from "./modal.admin";
import { ActionType } from "@ant-design/pro-components";
import { API_BASE_URL } from "service/api.config";
import Access from "views/admin/access";
type RowObj = {
  id: number;
  name: string;
  email: string;
  password: string;
  field: string;
  createAt: string;
  role: {
    id: number;
    name: string;
  };
};

interface TableTopCreatorsProps {
  tableData: RowObj[];
  reloadTable: () => void; // Nhận dữ liệu từ props
}

const TableTopCreators: React.FC<TableTopCreatorsProps> = ({ tableData, reloadTable }) => {
  const [openModal, setOpenModal] = useState<boolean>(false);
  const [dataInit, setDataInit] = useState<IAdmin | null>(null);
  const tableRef = useRef<ActionType>();
  const handleEdit = (row: IAdmin) => {
    setDataInit(row);
    setOpenModal(true);
    tableRef.current?.reset();
  };

  const handleCreate = () => {
    tableRef.current?.reset();
    setDataInit(null);
    setOpenModal(true);
  };
  const handleDelete = async (id: number) => {
    try {
      const response = await fetch(`${process.env.REACT_APP_API_BASE_URL}/api/v1/admins/${id}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      message.success("Delete admin successfully");
      reloadTable(); // Reload the table data
    } catch (error) {
      message.error("Failed to delete admin");
    }
  };
  return (
    <Card

      extra={"w-full sm:overflow-auto px-6"}>
      <header className="relative flex items-center justify-between pt-4">
        <div className="text-xl font-bold text-navy-700 dark:text-white">
          Admin Table
        </div>

        <button
          onClick={handleCreate}
          className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
        >
          Create account
        </button>


      </header>

      <div className="mt-0 overflow-x-scroll xl:overflow-x-hidden">
        <table className="w-full">
          <thead>
            <tr className="!border-px !border-gray-400">
              <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Name</th>
              <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Email</th>
              <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Field</th>
              <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Role</th>
              <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Actions</th>

            </tr>
          </thead>
          <tbody>
            {tableData.map((row, index) => (
              <tr key={index}>
                <td className="border-b border-gray-200 py-3 pr-4">{row.name}</td>
                <td className="border-b border-gray-200 py-3 pr-4">{row.email}</td>
                <td className="border-b border-gray-200 py-3 pr-4">{row.field}</td>
                <td className="border-b border-gray-200 py-3 pr-4">
                  {row.role.name}
                </td>
                <td className="border-b border-gray-200 py-3 pr-4">

                  <button onClick={() => handleEdit(row)} className="text-yellow-500 hover:underline ml-2">Update</button>


                  <button
                    onClick={() => handleDelete(row.id)}
                    className="text-red-500 hover:underline ml-2"
                  >Delete</button>

                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <ModalAdmin
        openModal={openModal}
        setOpenModal={setOpenModal}
        dataInit={dataInit}
        setDataInit={setDataInit}
        reloadTable={reloadTable}
      />
    </Card >
  );
};

export default TableTopCreators;