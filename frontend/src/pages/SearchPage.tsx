import React, { useEffect, useState } from "react";
import Header from "../components/Header";
import { searchByInterest, searchByAllInterests, searchByAnyInterest, api } from "../api";
import { User, Interest } from "../App";
import AddInterestModal from "../components/AddInterestModal";
import AddUserModal from "../components/AddUserModal";

const SearchPage: React.FC = () => {
    const [query, setQuery] = useState("");
    const [results, setResults] = useState<User[]>([]);
    const [loading, setLoading] = useState(false);
    const [mode, setMode] = useState<"one" | "all" | "any">("one");

    const [editModalOpen, setEditModalOpen] = useState(false);
    const [editingUser, setEditingUser] = useState<User | null>(null);
    const [editingInterest, setEditingInterest] = useState<Interest | null>(null);
    const [newInterestValue, setNewInterestValue] = useState("");

    const [addInterestModalOpen, setAddInterestModalOpen] = useState(false);

    const [addUserModalOpen, setAddUserModalOpen] = useState(false);

    const fetchAllUsers = async () => {
        setLoading(true);
        try {
            const res = await api.get<User[]>("/users");
            setResults(res.data);
        } catch {
            alert("Ошибка загрузки пользователей");
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = async () => {
        if (query.trim() === "") {
            fetchAllUsers();
            return;
        }
        setLoading(true);
        try {
            let res;
            const interestList = query
                .split(",")
                .map((s) => s.trim())
                .filter(Boolean);

            if (mode === "one") {
                res = await searchByInterest(query.trim());
            } else if (mode === "all") {
                res = await searchByAllInterests(interestList);
            } else {
                res = await searchByAnyInterest(interestList);
            }

            setResults(res.data);
        } catch {
            alert("Ошибка поиска");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAllUsers();
    }, []);

    const handleEditInterestClick = (user: User, interest: Interest) => {
        setEditingUser(user);
        setEditingInterest(interest);
        setNewInterestValue(interest.interestType);
        setEditModalOpen(true);
    };

    const handleDeleteInterest = async (userId: number, interestName: string) => {
        if (!window.confirm("Удалить интерес?")) return;
        try {
            await api.delete(`/users/${userId}/interests`, {
                params: { interestName },
            });
            if (query.trim() === "") {
                fetchAllUsers();
            } else {
                handleSearch();
            }
        } catch (err) {
            console.error(err);
            alert("Ошибка удаления интереса");
        }
    };

    const handleEditModalSave = async () => {
        if (!editingUser || !editingInterest) return;
        if (newInterestValue.trim() === "") {
            alert("Интерес не может быть пустым");
            return;
        }

        try {
            await api.put(`/users/${editingUser.id}/interests/${editingInterest.id}`, {
                interestType: newInterestValue.trim(),
            });
            setEditModalOpen(false);
            setEditingUser(null);
            setEditingInterest(null);
            setNewInterestValue("");
            if (query.trim() === "") {
                fetchAllUsers();
            } else {
                handleSearch();
            }
        } catch {
            alert("Ошибка сохранения интереса");
        }
    };

    const handleAddUserClick = () => {
        setAddUserModalOpen(true);
    };

    const handleUserAdded = (user: User) => {
        setResults((prev) => [user, ...prev]);
    };

    const handleAddInterestClick = () => {
        setAddInterestModalOpen(true);
    };

    return (
        <div
            style={{
                maxWidth: 960,
                margin: "20px auto",
                fontFamily: "'Segoe UI', sans-serif",
            }}
        >
            <Header
                onAddUserClick={handleAddUserClick}
                onAddInterestClick={handleAddInterestClick}
            />

            <h1>Поиск по интересам</h1>

            <div style={{ display: "flex", gap: "10px", marginBottom: "20px" }}>
                <input
                    type="text"
                    placeholder="Интерес(ы): чтение, бег, музыка..."
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    style={{
                        flex: 1,
                        padding: "10px",
                        borderRadius: "4px",
                        border: "1px solid #ccc",
                        fontSize: "16px",
                    }}
                />
                <select
                    value={mode}
                    onChange={(e) => setMode(e.target.value as "one" | "all" | "any")}
                    style={{
                        padding: "10px",
                        borderRadius: "4px",
                        fontSize: "16px",
                        border: "1px solid #ccc",
                    }}
                >
                    <option value="one">По одному</option>
                    <option value="all">По всем</option>
                    <option value="any">По любому</option>
                </select>
                <button
                    onClick={handleSearch}
                    style={{
                        padding: "10px 20px",
                        fontSize: "16px",
                        borderRadius: "4px",
                        border: "none",
                        backgroundColor: "#28a745",
                        color: "white",
                        cursor: "pointer",
                    }}
                >
                    Искать
                </button>
            </div>

            {loading && <p>Загрузка...</p>}

            <div
                style={{
                    display: "grid",
                    gridTemplateColumns: "repeat(auto-fill, minmax(220px, 1fr))",
                    gap: "20px",
                }}
            >
                {results.map((user) => (
                    <div
                        key={user.id}
                        style={{
                            padding: "15px",
                            backgroundColor: "#fff",
                            borderRadius: "10px",
                            boxShadow: "0 2px 8px rgba(0,0,0,0.15)",
                        }}
                    >
                        <h3 style={{ marginBottom: "4px" }}>{user.name}</h3>
                        <p style={{ margin: 0, fontSize: "14px", color: "#555" }}>{user.email}</p>
                        <p
                            style={{
                                margin: "4px 0",
                                fontSize: "13px",
                                color: "#999",
                            }}
                        >
                            Возраст: {user.age ?? "-"}
                        </p>
                        {user.interests && user.interests.length > 0 ? (
                            <div style={{ marginTop: "6px", fontSize: "13px", color: "#444" }}>
                                Интересы:{" "}
                                {user.interests.map((interest, idx) => (
                                    <span
                                        key={interest.id ?? idx}
                                        style={{
                                            marginLeft: idx === 0 ? 5 : 10,
                                            display: "inline-flex",
                                            alignItems: "center",
                                            gap: 4,
                                        }}
                                    >
                                        <>
                                            {interest.interestType || interest}
                                            <button
                                                onClick={() => handleEditInterestClick(user, interest)}
                                                title="Редактировать интерес"
                                                style={{
                                                    border: "none",
                                                    background: "none",
                                                    cursor: "pointer",
                                                    color: "#007bff",
                                                    fontSize: "14px",
                                                }}
                                            >
                                                ✏️
                                            </button>
                                            <button
                                                onClick={() => handleDeleteInterest(user.id, interest.interestType)}
                                                title="Удалить интерес"
                                                style={{
                                                    border: "none",
                                                    background: "none",
                                                    cursor: "pointer",
                                                    color: "red",
                                                    fontSize: "14px",
                                                }}
                                            >
                                                🗑️
                                            </button>
                                        </>
                                    </span>
                                ))}
                            </div>
                        ) : (
                            <p style={{ marginTop: 6, fontSize: "13px", color: "#777" }}>
                                Интересы не указаны
                            </p>
                        )}
                    </div>
                ))}
            </div>

            {/* Модалка редактирования интереса */}
            {editModalOpen && editingUser && editingInterest && (
                <div
                    style={{
                        position: "fixed",
                        top: 0,
                        left: 0,
                        width: "100vw",
                        height: "100vh",
                        backgroundColor: "rgba(0,0,0,0.5)",
                        display: "flex",
                        justifyContent: "center",
                        alignItems: "center",
                        zIndex: 9999,
                    }}
                    onClick={() => setEditModalOpen(false)}
                >
                    <div
                        style={{
                            backgroundColor: "white",
                            padding: "20px",
                            borderRadius: "8px",
                            minWidth: "300px",
                            boxShadow: "0 4px 12px rgba(0,0,0,0.3)",
                        }}
                        onClick={(e) => e.stopPropagation()}
                    >
                        <h2>Редактировать интерес</h2>
                        <input
                            type="text"
                            value={newInterestValue}
                            onChange={(e) => setNewInterestValue(e.target.value)}
                            style={{
                                width: "100%",
                                padding: "8px",
                                fontSize: "16px",
                                borderRadius: "4px",
                                border: "1px solid #ccc",
                                marginBottom: "12px",
                            }}
                            autoFocus
                        />
                        <div style={{ display: "flex", justifyContent: "flex-end", gap: 10 }}>
                            <button
                                onClick={() => setEditModalOpen(false)}
                                style={{
                                    padding: "8px 16px",
                                    fontSize: "16px",
                                    borderRadius: "4px",
                                    border: "1px solid #ccc",
                                    backgroundColor: "#f0f0f0",
                                    cursor: "pointer",
                                }}
                            >
                                Отмена
                            </button>
                            <button
                                onClick={handleEditModalSave}
                                style={{
                                    padding: "8px 16px",
                                    fontSize: "16px",
                                    borderRadius: "4px",
                                    border: "none",
                                    backgroundColor: "#28a745",
                                    color: "white",
                                    cursor: "pointer",
                                }}
                            >
                                Сохранить
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Модалка добавления пользователя через отдельный компонент */}
            {addUserModalOpen && (
                <AddUserModal
                    onClose={() => setAddUserModalOpen(false)}
                    onAdd={handleUserAdded}
                />
            )}

            {/* Модалка добавления интереса через отдельный компонент */}
            {addInterestModalOpen && (
                <AddInterestModal
                    onClose={() => setAddInterestModalOpen(false)}
                    onSuccess={() => {
                        setAddInterestModalOpen(false);
                        if (query.trim() === "") {
                            fetchAllUsers();
                        } else {
                            handleSearch();
                        }
                    }}
                />
            )}
        </div>
    );
};

export default SearchPage;
