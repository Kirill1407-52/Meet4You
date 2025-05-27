import React, { useState } from "react";
import { api } from "../api";
import { User } from "../App";

interface AddUserModalProps {
    onClose: () => void;
    onAdd: (user: User) => void;
}

const AddUserModal: React.FC<AddUserModalProps> = ({ onClose, onAdd }) => {
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [birth, setBirth] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
            const birthDate = birth ? new Date(birth).toISOString().split("T")[0] : null;
            const res = await api.post<User>("/users", {
                name,
                email,
                birth: birthDate,
            });

            onAdd(res.data);
            onClose();
        } catch (err: any) {
            setError(
                err.response?.data?.message || "Ошибка при создании пользователя. Попробуйте еще раз."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div
            style={{
                position: "fixed",
                top: 0, left: 0, right: 0, bottom: 0,
                backgroundColor: "rgba(0,0,0,0.4)",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                zIndex: 1000,
            }}
            onClick={onClose}
        >
            <div
                style={{
                    backgroundColor: "white",
                    padding: "30px",
                    borderRadius: "8px",
                    width: "320px",
                    boxShadow: "0 4px 12px rgba(0,0,0,0.3)",
                }}
                onClick={e => e.stopPropagation()} // Останавливаем закрытие при клике внутри окна
            >
                <h2 style={{ marginTop: 0 }}>Добавить пользователя</h2>

                {error && (
                    <div style={{ color: "red", marginBottom: "12px" }}>
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    <label style={{ display: "block", marginBottom: "8px" }}>
                        Имя:
                        <input
                            type="text"
                            value={name}
                            onChange={e => setName(e.target.value)}
                            required
                            disabled={loading}
                            style={{
                                width: "100%",
                                padding: "8px",
                                marginTop: "4px",
                                marginBottom: "12px",
                                borderRadius: "4px",
                                border: "1px solid #ccc",
                            }}
                        />
                    </label>

                    <label style={{ display: "block", marginBottom: "8px" }}>
                        Email:
                        <input
                            type="email"
                            value={email}
                            onChange={e => setEmail(e.target.value)}
                            required
                            disabled={loading}
                            style={{
                                width: "100%",
                                padding: "8px",
                                marginTop: "4px",
                                marginBottom: "12px",
                                borderRadius: "4px",
                                border: "1px solid #ccc",
                            }}
                        />
                    </label>

                    <label style={{ display: "block", marginBottom: "8px" }}>
                        Дата рождения:
                        <input
                            type="date"
                            value={birth}
                            onChange={e => setBirth(e.target.value)}
                            disabled={loading}
                            style={{
                                width: "100%",
                                padding: "8px",
                                marginTop: "4px",
                                marginBottom: "12px",
                                borderRadius: "4px",
                                border: "1px solid #ccc",
                            }}
                        />
                    </label>

                    <div style={{ display: "flex", justifyContent: "flex-end", gap: "10px" }}>
                        <button
                            type="button"
                            onClick={onClose}
                            disabled={loading}
                            style={{
                                padding: "8px 16px",
                                borderRadius: "4px",
                                border: "1px solid #ccc",
                                backgroundColor: "#eee",
                                cursor: loading ? "not-allowed" : "pointer",
                            }}
                        >
                            Отмена
                        </button>
                        <button
                            type="submit"
                            disabled={loading}
                            style={{
                                padding: "8px 16px",
                                borderRadius: "4px",
                                border: "none",
                                backgroundColor: loading ? "#6c757d" : "#007bff",
                                color: "white",
                                cursor: loading ? "not-allowed" : "pointer",
                            }}
                        >
                            {loading ? "Добавляем..." : "Добавить"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default AddUserModal;
