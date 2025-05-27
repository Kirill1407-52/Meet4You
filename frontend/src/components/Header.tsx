import React from "react";

interface HeaderProps {
    onAddUserClick: () => void;
    onAddInterestClick: () => void;
}

const Header: React.FC<HeaderProps> = ({ onAddUserClick, onAddInterestClick }) => {
    const buttonStyle = {
        padding: "8px 16px",
        fontSize: "14px",
        borderRadius: "5px",
        cursor: "pointer",
        border: "none",
        color: "white",
    };

    return (
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
            <div style={{ fontSize: "24px", fontWeight: "bold", color: "#333" }}>
                MeetYou
            </div>

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
                    onClick={onAddUserClick}
                    style={{
                        ...buttonStyle,
                        backgroundColor: "#28a745",
                    }}
                >
                    + Добавить пользователя
                </button>
                <button
                    onClick={onAddInterestClick}
                    style={{
                        ...buttonStyle,
                        backgroundColor: "#17a2b8",
                    }}
                >
                    + Добавить интерес
                </button>
            </div>
        </header>
    );
};

export default Header;
