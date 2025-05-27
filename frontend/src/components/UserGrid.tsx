import React from "react";
import { User } from "../App";

interface UserGridProps {
    users: User[];
    onEdit: (user: User) => void;
    onDelete: (userId: number) => void;
}

const UserGrid: React.FC<UserGridProps> = ({ users, onEdit, onDelete }) => {
    return (
        <div
            style={{
                display: "grid",
                gridTemplateColumns: "repeat(auto-fill,minmax(220px,1fr))",
                gap: "20px",
                marginTop: "20px",
            }}
        >
            {users.map((user) => (
                <div
                    key={user.id}
                    style={{
                        borderRadius: "10px",
                        boxShadow: "0 2px 8px rgba(0,0,0,0.15)",
                        padding: "15px",
                        backgroundColor: "#fff",
                        display: "flex",
                        flexDirection: "column",
                        alignItems: "center",
                        position: "relative",
                        height: "100%",
                    }}
                >
                    {/* Контейнер для основного контента */}
                    <div
                        style={{
                            display: "flex",
                            flexDirection: "column",
                            alignItems: "center",
                            justifyContent: "center",
                            flex: 1,
                            width: "100%",
                        }}
                    >
                        <div
                            style={{
                                width: "100px",
                                height: "100px",
                                borderRadius: "50%",
                                backgroundColor: "#eee",
                                display: "flex",
                                justifyContent: "center",
                                alignItems: "center",
                                fontSize: "36px",
                                color: "#888",
                                marginBottom: "10px",
                                userSelect: "none",
                            }}
                        >
                            {user.name.charAt(0).toUpperCase()}
                        </div>
                        <h3 style={{ margin: "0 0 8px 0", textAlign: "center" }}>{user.name}</h3>
                        <p style={{ margin: "0", fontSize: "14px", color: "#555", textAlign: "center" }}>
                            {user.email}
                        </p>
                        <p style={{ margin: "5px 0 0 0", fontSize: "13px", color: "#999", textAlign: "center" }}>
                            Возраст: {user.age ?? "-"}
                        </p>

                        {user.interests && user.interests.length > 0 && (
                            <p
                                style={{
                                    marginTop: "10px",
                                    fontSize: "13px",
                                    color: "#444",
                                    textAlign: "center",
                                }}
                            >
                                Интересы: {user.interests.map((i) => i.interestType).join(", ")}
                            </p>
                        )}
                    </div>
                    {/* Кнопки действий */}
                    <div
                        style={{
                            marginTop: "15px",
                            width: "100%",
                            display: "flex",
                            justifyContent: "space-between",
                        }}
                    >
                        <button
                            onClick={() => onEdit(user)}
                            title="Редактировать"
                            style={{
                                border: "none",
                                background: "none",
                                cursor: "pointer",
                                fontSize: "18px",
                            }}
                            aria-label={'Редактировать пользователя ${user.name}'}
                        >
                            ✏️
                        </button>
                        <button
                            onClick={() => onDelete(user.id)}
                            title="Удалить"
                            style={{
                                border: "none",
                                background: "none",
                                cursor: "pointer",
                                fontSize: "18px",
                                color: "red",
                            }}
                            aria-label={'Удалить пользователя ${user.name}'}
                        >
                            🗑
                        </button>
                    </div>
                </div>
            ))}
        </div>
    );
};

export default UserGrid;