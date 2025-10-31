import { Layout } from 'antd'

const { Header } = Layout

export default function HeaderBar() {
    return (
        <Header
            style={{
                background: '#fff',
                paddingLeft: 16,
                fontWeight: 500,
            }}
        >
            👋 Администратор
        </Header>
    )
}
