import React, { useEffect, useState } from "react";
import { User } from "../App";
import { api } from "../api";

interface EditUserModalProps {
    user: User;
    onClose: () => void;
    onUpdate: (updatedUser: User) => void;
}

const EditUserModal: React.FC<EditUserModalProps> = ({ user, onClose, onUpdate }) => {
    const [name, setName] = useState(user.name);
    const [email, setEmail] = useState(user.email);
    const [birth, setBirth] = useState(user.birth);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        setName(user.name);
        setEmail(user.email);
        setBirth(user.birth);
        setError(null);
    }, [user]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
            const birthDate = birth ? new Date(birth).toISOString().split("T")[0] : "";

            const updatePayload: Partial<User> = {
                name: name.trim(),
                email: email.trim(),
                birth: birthDate,
            };

            const res = await api.put<User>(`/users/${user.id}`, updatePayload);

            const updatedUser: User = {
                ...user,
                ...updatePayload,
                ...res.data,
            };

            onUpdate(updatedUser);
            onClose();
        } catch (err: any) {
            setError(
                err.response?.data?.message ||
                "Ошибка при обновлении пользователя. Попробуйте ещё раз."
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
                onClick={e => e.stopPropagation()}
            >
                <h2 style={{ marginTop: 0 }}>Редактировать пользователя</h2>

                {error && (
                    <div style={{ color: "red", marginBottom: "12px" }}>{error}</div>
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
                            value={birth ? new Date(birth).toISOString().split("T")[0] : ""}
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
                                backgroundColor: loading ? "#6c757d" : "#28a745",
                                color: "white",
                                cursor: loading ? "not-allowed" : "pointer",
                            }}
                        >
                            {loading ? "Сохраняем..." : "Сохранить"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default EditUserModal;
