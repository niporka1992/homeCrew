import { Layout, Menu, theme } from 'antd'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import {
    DashboardOutlined,
    TeamOutlined,
    CheckCircleOutlined,
} from '@ant-design/icons'

const { Header, Content, Footer } = Layout

export default function AppLayout() {
    const { token } = theme.useToken()
    const location = useLocation()
    const navigate = useNavigate()

    const current = location.pathname.startsWith('/users')
        ? 'users'
        : location.pathname.startsWith('/tasks')
            ? 'tasks'
            : 'dashboard'

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

    return (
        <div
            style={{
                width: '100%',
                minHeight: '100dvh', // ✅ учитывает мобильные браузеры
                overflowX: 'hidden',
                overflowY: 'auto',
                background: '#0d0d0d', // тянет фон до краёв
            }}
        >
            <Layout
                style={{
                    minHeight: '100%',
                    background: '#0d0d0d',
                }}
            >
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
