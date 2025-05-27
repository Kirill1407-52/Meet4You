import React, { useEffect, useState } from "react";
import { api } from "./api";
import UserGrid from "./components/UserGrid";
import AddUserModal from "./components/AddUserModal";
import EditUserModal from "./components/EditUserModal";
import AddInterestModal from "./components/AddInterestModal";

export interface Interest {
    id: number;
    interestType: string;
}

export interface User {
    id: number;
    name: string;
    email: string;
    birth: string;
    age?: number;
    interests?: Interest[];
}

const App: React.FC = () => {
    const [users, setUsers] = useState<User[]>([]);
    const [isUserModalOpen, setIsUserModalOpen] = useState(false);
    const [editingUser, setEditingUser] = useState<User | null>(null);
    const [isInterestModalOpen, setIsInterestModalOpen] = useState(false);


    const fetchUsers = async () => {
        try {
            const res = await api.get<User[]>("/users");
            setUsers(res.data);
        } catch {
            alert("Ошибка загрузки пользователей");
        }
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    const handleAddUser = (newUser: User) => {
        setUsers((prev) => [...prev, newUser]);
        setIsUserModalOpen(false);
    };

    const handleUpdateUser = (updatedUser: User) => {
        setUsers((prev) =>
            prev.map((user) => (user.id === updatedUser.id ? updatedUser : user))
        );
        setEditingUser(null);
    };

    const handleDeleteUser = async (userId: number) => {
        if (!window.confirm("Удалить пользователя?")) return;
        try {
            await api.delete(`/users/${userId}`);
            setUsers((prev) => prev.filter((user) => user.id !== userId));
        } catch {
            alert("Ошибка удаления");
        }
    };
    
    const buttonStyle = {
        padding: "8px 16px",
        fontSize: "14px",
        borderRadius: "5px",
        cursor: "pointer",
        border: "none",
        color: "white",
    };

    return (
        <div
            style={{
                maxWidth: 960,
                margin: "20px auto",
                fontFamily: "'Segoe UI', Tahoma, Geneva, Verdana, sans-serif",
            }}
        >
            {/* Хедер */}
            <header
                style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    paddingBottom: "20px",
                    borderBottom: "1px solid #ddd",
                    marginBottom: "20px",
                }}
            >
                {/* Название */}
                <div style={{ fontSize: "24px", fontWeight: "bold", color: "#333" }}>
                    MeetYou
                </div>

                {/* Кнопки */}
                <div style={{ display: "flex", gap: "10px" }}>
                    <button
                        onClick={() => (window.location.href = "/")}
                        style={{
                            padding: "8px 16px",
                            fontSize: "14px",
                            borderRadius: "5px",
                            border: "1px solid #ccc",
                            backgroundColor: "#f8f9fa",
                            cursor: "pointer",
                            color: "#000",
                        }}
                    >
                        Профили
                    </button>
                    <button
                        onClick={() => (window.location.href = "/search")}
                        style={{
                            ...buttonStyle,
                            backgroundColor: "#007bff",
                        }}
                    >
                        Поиск по критериям
                    </button>
                    <button
                        onClick={() => setIsUserModalOpen(true)}
                        style={{
                            ...buttonStyle,
                            backgroundColor: "#28a745",
                        }}
                    >
                        + Добавить пользователя
                    </button>
                    <button
                        onClick={() => setIsInterestModalOpen(true)}
                        style={{
                            ...buttonStyle,
                            backgroundColor: "#17a2b8",
                        }}
                    >
                        + Добавить интерес
                    </button>
                </div>
            </header>

            {/* Сетка пользователей */}
            <UserGrid users={users} onEdit={setEditingUser} onDelete={handleDeleteUser} />

            {/* Модалки */}
            {isUserModalOpen && (
                <AddUserModal onClose={() => setIsUserModalOpen(false)} onAdd={handleAddUser} />
            )}

            {editingUser && (
                <EditUserModal
                    user={editingUser}
                    onClose={() => setEditingUser(null)}
                    onUpdate={handleUpdateUser}
                />
            )}

            {isInterestModalOpen && (
                <AddInterestModal
                    onClose={() => setIsInterestModalOpen(false)}
                    onSuccess={() => {
                        setIsInterestModalOpen(false);
                        fetchUsers();
                    }}
                />
            )}
        </div>
    );
};

export default App;
