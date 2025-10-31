import { Layout, Menu } from 'antd'
import { Link, useLocation } from 'react-router-dom'

const { Sider } = Layout

export default function Sidebar() {
    const location = useLocation()
    const selectedKey = location.pathname

    return (
        <Sider theme="dark" width={200}>
            <div style={{ color: 'white', padding: 16, fontWeight: 'bold' }}>
                HomeCrew Admin
            </div>
            <Menu theme="dark" mode="inline" selectedKeys={[selectedKey]}>
                <Menu.Item key="/">
                    <Link to="/">Панель</Link>
                </Menu.Item>
                <Menu.Item key="/users">
                    <Link to="/users">Пользователи</Link>
                </Menu.Item>
            </Menu>
        </Sider>
    )
}
