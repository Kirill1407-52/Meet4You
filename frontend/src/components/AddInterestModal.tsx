import React, { useEffect, useState } from "react";
import { api } from "../api";
import { User } from "../App";

interface Props {
    onClose: () => void;
    onSuccess?: () => void;
}

const AddInterestModal: React.FC<Props> = ({ onClose, onSuccess }) => {
    const [users, setUsers] = useState<User[]>([]);
    const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
    const [interest, setInterest] = useState("");

    useEffect(() => {
        api.get<User[]>("/users").then(res => setUsers(res.data));
    }, []);

    const handleSubmit = async () => {
        if (!selectedUserId || !interest.trim()) {
            alert("Выберите пользователя и введите интерес");
            return;
        }

        try {
            await api.post(`/users/${selectedUserId}/interests`, null, {
                params: { interestName: interest.trim() },
            });
            alert("Интерес добавлен!");
            onClose();
            onSuccess?.();
        } catch (err) {
            alert("Ошибка при добавлении интереса");
        }
    };

    return (
        <div style={{
            position: "fixed", top: 0, left: 0, right: 0, bottom: 0,
            backgroundColor: "rgba(0,0,0,0.3)", display: "flex", justifyContent: "center", alignItems: "center"
        }}>
            <div style={{
                background: "#fff", padding: 20, borderRadius: 10, width: 400, boxShadow: "0 4px 12px rgba(0,0,0,0.2)"
            }}>
                <h2>Добавить интерес</h2>

                <div style={{ marginBottom: 12 }}>
                    <label>Пользователь:</label>
                    <select
                        value={selectedUserId ?? ""}
                        onChange={(e) => setSelectedUserId(Number(e.target.value))}
                        style={{ width: "100%", padding: 8, marginTop: 4 }}
                    >
                        <option value="">-- выберите --</option>
                        {users.map(user => (
                            <option key={user.id} value={user.id}>{user.name}</option>
                        ))}
                    </select>
                </div>

                <div style={{ marginBottom: 12 }}>
                    <label>Интерес:</label>
                    <input
                        value={interest}
                        onChange={e => setInterest(e.target.value)}
                        style={{ width: "100%", padding: 8, marginTop: 4 }}
                    />
                </div>

                <div style={{ display: "flex", justifyContent: "flex-end", gap: 8 }}>
                    <button onClick={onClose}>Отмена</button>
                    <button
                        onClick={handleSubmit}
                        style={{ backgroundColor: "#007bff", color: "white", border: "none", padding: "8px 16px", borderRadius: 4 }}
                    >
                        Добавить
                    </button>
                </div>
            </div>
        </div>
    );
};

export default AddInterestModal;
