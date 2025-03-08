import React, { useEffect, useState } from "react";
import Card from "components/card";
import { API_BASE_URL, API_ENDPOINTS } from "service/api.config"; // Import API config
import { useAuth } from "hooks/useAuth"; // Import useAuth hook
import { useNavigate } from "react-router-dom";
import Access from "views/admin/access";
import { message } from "antd";

type RowObj = {
    id: number;
    name: string;
    email: string;
    phone: string;
    speciField: string;
    englishlevel: string;
};

const TableUser: React.FC = () => {
    const { token } = useAuth(); // Lấy token từ context
    const [tableData, setTableData] = useState<RowObj[]>([]);
    // const [modalIsOpen, setModalIsOpen] = useState(false);
    // const [newUser, setNewUser] = useState({ name: "", email: "", password: "", field: "", engLevel: 0 });
    const navigate = useNavigate();
    useEffect(() => {
        const fetchData = async () => {
            try {
                const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.ADMIN.MANAGE_USERS}`, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`, // Sử dụng token từ context
                        'Content-Type': 'application/json',
                    },
                });
                const data = await response.json();
                if (data.result && Array.isArray(data.result)) {
                    setTableData(data.result);
                } else {
                    console.error("Data is not an array:", data);
                }
            } catch (error) {
                console.error("Error fetching data:", error);
            }
        };

        if (token) {
            fetchData(); // Gọi fetchData nếu có token
        }
    }, [token]); // Chạy lại khi token thay đổi

    const handleView = (userId: number) => {
        navigate(`/admin/profile/${userId}`); // Điều hướng đến trang profile
    };

    const handleDelete = async (id: number) => {
        if (!id) return;
        try {
            const response = await fetch(`${API_BASE_URL}/api/v1/users/${id}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });
            if (response.ok) {
                setTableData(tableData.filter(row => row.id !== id));
                console.log("User deleted successfully");
            } else {
                message.error("Failed to delete user");
            }
        } catch (error) {
            console.error("Error deleting user:", error);
        }
    };


    return (
        <Card extra={"w-full sm:overflow-auto px-6"}>
            <header className="relative flex items-center justify-between pt-4">
                <div className="text-xl font-bold text-navy-700 dark:text-white">
                    User Table
                </div>
            </header>

            <div className="mt-0 overflow-x-scroll xl:overflow-x-hidden">
                <table className="w-full" >
                    <thead>
                        <tr className="!border-px !border-gray-400">
                            <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Name</th>
                            <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Email</th>
                            <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Phone</th>
                            <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Field</th>
                            <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">English Level</th>
                            <th className="border-b border-gray-200 pb-2 pr-4 pt-4 text-start">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {tableData.map((row, index) => (
                            <tr key={index}>
                                <td className="border-b border-gray-200 py-3 pr-4">{row.name}</td>
                                <td className="border-b border-gray-200 py-3 pr-4">{row.email}</td>
                                <td className="border-b border-gray-200 py-3 pr-4">{row.phone}</td>
                                <td className="border-b border-gray-200 py-3 pr-4">{row.speciField}</td>
                                <td className="border-b border-gray-200 py-3 pr-4">{row.englishlevel}</td>
                                <td className="border-b border-gray-200 py-3 pr-4">
                                    <button onClick={() => handleView(row.id)} className="text-blue-500 hover:underline">View</button>
                                    <Access
                                        permission={{ module: "SYSTEM_MANAGEMENT" }}
                                        hideChildren={true}
                                    >
                                        <button onClick={() => handleDelete(row.id)} className="text-red-500 hover:underline ml-2">Delete</button>
                                    </Access>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>


        </Card>
    );
};

export default TableUser;