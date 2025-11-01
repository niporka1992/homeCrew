import { Layout, Menu, Button, Space, theme, App, Tooltip } from 'antd'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import {
    DashboardOutlined,
    TeamOutlined,
    CheckCircleOutlined,
    LogoutOutlined,
} from '@ant-design/icons'

const { Header, Content, Footer } = Layout

export default function AppLayout() {
    const { token } = theme.useToken()
    const location = useLocation()
    const navigate = useNavigate()
    const { message } = App.useApp()

    // === Определяем активный пункт меню ===
    const current = location.pathname.startsWith('/users')
        ? 'users'
        : location.pathname.startsWith('/tasks')
            ? 'tasks'
            : 'dashboard'

    // === Пункты меню ===
    const items = [
        {
            key: 'dashboard',
            icon: <DashboardOutlined />,
            label: 'Планировщик',
            onClick: () => navigate('/'),
        },
        {
            key: 'users',
            icon: <TeamOutlined />,
            label: 'Пользователи',
            onClick: () => navigate('/users'),
        },
        {
            key: 'tasks',
            icon: <CheckCircleOutlined />,
            label: 'Задачи',
            onClick: () => navigate('/tasks'),
        },
    ]

    // === Клиентский logout ===
    const handleLogout = () => {
        localStorage.removeItem('token')
        message.info('Вы вышли из системы')
        navigate('/login')
    }

    return (
        <div
            style={{
                width: '100%',
                minHeight: '100dvh',
                overflowX: 'hidden',
                overflowY: 'auto',
                background: '#0d0d0d',
            }}
        >
            <Layout style={{ minHeight: '100%', background: '#0d0d0d' }}>
                <Header
                    style={{
                        background: '#141414',
                        display: 'flex',
                        alignItems: 'center',
                        padding: '0 24px',
                        borderBottom: `1px solid ${token.colorBorderSecondary}`,
                        position: 'relative',
                        zIndex: 2,
                    }}
                >
                    <Menu
                        theme="dark"
                        mode="horizontal"
                        selectedKeys={[current]}
                        items={items}
                        style={{
                            flex: 1,
                            background: 'transparent',
                            borderBottom: 'none',
                            minWidth: 0,
                        }}
                    />

                    {/* 🔹 Кнопка "Выйти" справа */}
                    <Space>
                        <Tooltip title="Выйти из системы">
                            <Button
                                type="text"
                                icon={<LogoutOutlined style={{ color: '#ff7875', fontSize: 18 }} />}
                                onClick={handleLogout}
                                style={{
                                    color: '#ff7875',
                                    fontWeight: 500,
                                }}
                            >
                                Выйти
                            </Button>
                        </Tooltip>
                    </Space>
                </Header>

                <Content
                    style={{
                        display: 'flex',
                        justifyContent: 'center',
                        background: '#141414',
                        padding: '32px 0',
                        flex: 1,
                        overflowX: 'hidden',
                    }}
                >
                    <div
                        style={{
                            width: '100%',
                            maxWidth: 1400,
                            padding: '0 24px',
                            boxSizing: 'border-box',
                        }}
                    >
                        <Outlet />
                    </div>
                </Content>

                <Footer
                    style={{
                        textAlign: 'center',
                        color: token.colorTextTertiary,
                        background: '#141414',
                        borderTop: `1px solid ${token.colorBorderSecondary}`,
                        padding: '12px 24px',
                    }}
                >
                    © 2025 HomeCrew
                </Footer>
            </Layout>
        </div>
    )
}
