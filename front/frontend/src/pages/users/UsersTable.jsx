// src/pages/users/UsersTable.jsx
import { useState, useEffect, useMemo } from 'react'
import {
    Card,
    Table,
    Tag,
    Typography,
    Button,
    Space,
    Grid,
    Tooltip,
    message,
    Switch,
} from 'antd'
import {
    EditOutlined,
    LockOutlined,
    UnlockOutlined,
    KeyOutlined,
} from '@ant-design/icons'
import { api } from '../../api/api.js'

const { Title, Text } = Typography
const { useBreakpoint } = Grid

export default function UsersTable({ users, loading, onEdit, onChangePassword }) {
    const screens = useBreakpoint()
    const isMobile = !screens.md
    const [localUsers, setLocalUsers] = useState(users)
    const [showContacts, setShowContacts] = useState(false)

    useEffect(() => setLocalUsers(users), [users])

    // =========================================================
    // 🔹 Блокировка / разблокировка пользователя
    // =========================================================
    const handleToggleBlock = async (user) => {
        const newStatus = !user.blocked
        try {
            await api.patch(
                `/users/${encodeURIComponent(user.username)}/status?isBlocked=${newStatus}`
            )

            setLocalUsers((prev) =>
                prev.map((u) =>
                    u.username === user.username ? { ...u, blocked: newStatus } : u
                )
            )

            message.success(
                newStatus
                    ? 'Пользователь заблокирован'
                    : 'Пользователь разблокирован'
            )
        } catch (e) {
            console.error(e)
            message.error(e.message || 'Ошибка при изменении статуса')
        }
    }

    // =========================================================
    // 🔹 Колонки таблицы
    // =========================================================
    const columns = useMemo(() => {
        const baseCols = [
            {
                title: 'Имя пользователя',
                dataIndex: 'username',
                key: 'username',
                align: 'center',
                render: (text) => (
                    <Text strong style={{ color: '#40a9ff' }}>
                        {text}
                    </Text>
                ),
            },
            {
                title: 'Роль',
                dataIndex: 'role',
                key: 'role',
                align: 'center',
                render: (role) => {
                    const map = {
                        OWNER: { color: 'geekblue', label: 'Администратор' },
                        WORKER: { color: 'green', label: 'Работник' },
                        GUEST: { color: 'orange', label: 'Гость' },
                    }
                    const { color, label } = map[role] || { color: 'default', label: role }
                    return <Tag color={color}>{label}</Tag>
                },
            },
        ]

        const contactCols = showContacts
            ? [
                {
                    title: 'Телефон',
                    dataIndex: 'phone',
                    key: 'phone',
                    align: 'center',
                    render: (phone) =>
                        phone ? (
                            <Tooltip title="Позвонить">
                                <a
                                    href={`tel:${phone.replace(/[^\d+]/g, '')}`}
                                    style={{
                                        color: '#40a9ff',
                                        textDecoration: 'none',
                                    }}
                                >
                                    {phone}
                                </a>
                            </Tooltip>
                        ) : (
                            <Text type="secondary">—</Text>
                        ),
                },
                {
                    title: 'Email',
                    dataIndex: 'email',
                    key: 'email',
                    align: 'center',
                    render: (v) =>
                        v ? (
                            <Tooltip title={v}>
                                <Text
                                    style={{
                                        color: '#d9d9d9',
                                        fontFamily: 'monospace',
                                        cursor: 'pointer',
                                    }}
                                >
                                    {v}
                                </Text>
                            </Tooltip>
                        ) : (
                            <Text type="secondary">—</Text>
                        ),
                },
            ]
            : []

        const actionsCol = {
            title: 'Действия',
            key: 'actions',
            align: 'center',
            width: 180,
            render: (_, user) => (
                <Space
                    size="small"
                    align="center"
                    style={{ justifyContent: 'center' }}
                >
                    <Tooltip title="Редактировать">
                        <Button
                            icon={<EditOutlined />}
                            shape="circle"
                            type="text"
                            size="small"
                            onClick={() => onEdit(user)}
                            style={{
                                color: '#69b1ff',
                                background: '#141414',
                                border: '1px solid #303030',
                            }}
                        />
                    </Tooltip>

                    <Tooltip title="Сменить пароль">
                        <Button
                            icon={<KeyOutlined />}
                            shape="circle"
                            type="text"
                            size="small"
                            onClick={() => onChangePassword(user)}
                            style={{
                                color: '#ffa940',
                                background: '#141414',
                                border: '1px solid #303030',
                            }}
                        />
                    </Tooltip>

                    <Tooltip
                        title={user.blocked ? 'Разблокировать' : 'Заблокировать'}
                    >
                        <Button
                            icon={user.blocked ? <UnlockOutlined /> : <LockOutlined />}
                            shape="circle"
                            size="small"
                            onClick={() => handleToggleBlock(user)}
                            style={{
                                background: user.blocked ? '#ff4d4f' : '#177ddc',
                                border: 'none',
                                color: '#fff',
                                boxShadow: user.blocked
                                    ? '0 0 8px rgba(255,77,79,0.6)'
                                    : '0 0 8px rgba(23,125,220,0.4)',
                                transition: 'all 0.25s ease',
                            }}
                        />
                    </Tooltip>
                </Space>
            ),
        }

        return [...baseCols, ...contactCols, actionsCol]
    }, [showContacts, onEdit, onChangePassword])

    // =========================================================
    // 🔹 Рендер
    // =========================================================
    return (
        <div
            style={{
                display: 'flex',
                justifyContent: 'center',
                background: '#141414',
                padding: '2rem 1rem',
                boxSizing: 'border-box',
                minHeight: '100vh',
            }}
        >
            <div
                style={{
                    width: '100%',
                    maxWidth: '1400px',
                }}
            >
                <div
                    style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        marginBottom: 20,
                        flexWrap: 'wrap',
                    }}
                >
                    <Title
                        level={3}
                        style={{
                            color: '#e6f4ff',
                            margin: 0,
                        }}
                    >
                        👥 Пользователи системы
                    </Title>

                    <div
                        style={{
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: 'center',
                            gap: 4,
                        }}
                    >
                        <Switch
                            checked={showContacts}
                            onChange={setShowContacts}
                            checkedChildren="ON"
                            unCheckedChildren="OFF"
                            style={{
                                backgroundColor: showContacts ? '#177ddc' : '#555',
                                boxShadow: '0 0 8px rgba(0,0,0,0.4)',
                            }}
                        />
                        <Text style={{ color: '#aaa', fontSize: 12 }}>
                            Показывать контакты
                        </Text>
                    </div>
                </div>

                <Card
                    bordered={false}
                    style={{
                        width: '100%',
                        background: '#1f1f1f',
                        borderRadius: 12,
                        color: '#fff',
                        boxShadow: '0 2px 12px rgba(0,0,0,0.5)',
                    }}
                >
                    <Table
                        columns={columns}
                        dataSource={localUsers}
                        rowKey={(r, i) => r.id ?? r.username ?? i}
                        loading={loading}
                        pagination={{
                            pageSize: 10,
                            position: ['bottomCenter'],
                            showSizeChanger: false,
                        }}
                        bordered
                        size={isMobile ? 'small' : 'middle'}
                        scroll={{ x: true }}
                        style={{
                            width: '100%',
                            background: '#1f1f1f',
                        }}
                    />
                </Card>
            </div>
        </div>
    )
}
