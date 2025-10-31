import { useEffect, useState } from 'react'
import {
    Card,
    Typography,
    Table,
    Button,
    Space,
    Select,
    Tag,
    message,
} from 'antd'
import { ReloadOutlined, EyeOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import TaskDetailsModal from './TaskDetailsModal'
import { api } from '../../api/api.js'
const { Title, Text } = Typography

export default function TasksContainer() {
    const [tasks, setTasks] = useState([])
    const [users, setUsers] = useState([])
    const [status, setStatus] = useState('')
    const [selectedUserFullName, setSelectedUserFullName] = useState(null)
    const [loading, setLoading] = useState(false)

    const [selectedTaskId, setSelectedTaskId] = useState(null)
    const [isModalOpen, setIsModalOpen] = useState(false)

// ==========================
// 🔁 Загрузка задач
// ==========================
    const loadTasks = async () => {
        setLoading(true)
        try {
            const data = await api.get('/tasks')
            setTasks(data)
        } catch (err) {
            console.error('Ошибка при загрузке задач:', err)
            message.error(err.message || 'Не удалось загрузить задачи')
        } finally {
            setLoading(false)
        }
    }

// ==========================
// 🔁 Загрузка пользователей
// ==========================
    const loadUsers = async () => {
        try {
            const data = await api.get('/users?role=WORKER')
            setUsers(data)
        } catch (err) {
            console.error('Ошибка при загрузке пользователей:', err)
            message.error(err.message || 'Не удалось загрузить пользователей')
        }
    }


    // ==========================
    // 📦 useEffect
    // ==========================
    useEffect(() => {
        loadUsers()
        loadTasks()
    }, [])

    // ==========================
    // ⚙️ Фильтрация на фронте
    // ==========================
    const filteredTasks = tasks.filter((t) => {
        const statusMatch = !status || t.status === status
        const userMatch =
            !selectedUserFullName || t.assigneeFullName === selectedUserFullName
        return statusMatch && userMatch
    })

    // ==========================
    // 🏷️ Статусные метки
    // ==========================
    const statusTag = (status) => {
        const colorMap = {
            NEW: 'blue',
            IN_PROGRESS: 'gold',
            DONE: 'green',
            CANCELED: 'red',
        }
        const labelMap = {
            NEW: 'Новая',
            IN_PROGRESS: 'В работе',
            DONE: 'Завершена',
            CANCELED: 'Отменена',
        }
        return (
            <Tag color={colorMap[status] || 'default'}>
                {labelMap[status] || status}
            </Tag>
        )
    }

    // ==========================
    // 📋 Колонки таблицы
    // ==========================
    const columns = [
        {
            title: 'Описание',
            dataIndex: 'description',
            key: 'description',
            render: (text) => text || <Text type="secondary">—</Text>,
        },
        {
            title: 'Исполнитель',
            dataIndex: 'assigneeFullName',
            key: 'assigneeFullName',
            align: 'center',
            render: (v) => v || <Text type="secondary">Не назначен</Text>,
        },
        {
            title: 'Статус',
            dataIndex: 'status',
            key: 'status',
            align: 'center',
            render: (v) => statusTag(v),
        },
        {
            title: 'Создана',
            dataIndex: 'dateOfCreate',
            key: 'dateOfCreate',
            align: 'center',
            render: (v) => (v ? dayjs(v).format('DD.MM.YYYY HH:mm') : '—'),
        },
        {
            key: 'actions',
            align: 'center',
            width: 60,
            render: (_, record) => (
                <Button
                    type="text"
                    icon={<EyeOutlined style={{ color: '#1677ff', fontSize: 18 }} />}
                    onClick={(e) => {
                        e.stopPropagation()
                        openTaskModal(record.id)
                    }}
                />
            ),
        },
    ]

    // ==========================
    // 🔍 Открытие модалки
    // ==========================
    const openTaskModal = (taskId) => {
        setSelectedTaskId(taskId)
        setIsModalOpen(true)
    }

    // ==========================
    // 🧩 UI
    // ==========================
    return (
        <>
            <Card
                bordered
                style={{
                    margin: '1rem auto',
                    borderRadius: 16,
                    background: '#141414',
                    color: '#fff',
                    boxShadow: '0 2px 12px rgba(0,0,0,0.4)',
                    maxWidth: 1200,
                }}
            >
                <div
                    style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        marginBottom: 16,
                    }}
                >
                    <Title level={3} style={{ color: '#e6f4ff', margin: 4 }}>
                        📋 Задачи
                    </Title>

                    <Space>
                        <Select
                            value={status || undefined}
                            onChange={(val) => setStatus(val || '')}
                            placeholder="Статус"
                            style={{ width: 180 }}
                            allowClear
                            options={[
                                { value: 'NEW', label: 'Новые' },
                                { value: 'IN_PROGRESS', label: 'В работе' },
                                { value: 'DONE', label: 'Завершённые' },
                            ]}
                        />

                        <Select
                            value={selectedUserFullName ?? undefined}
                            onChange={(val) => setSelectedUserFullName(val ?? null)}
                            placeholder="Все пользователи"
                            style={{ width: 220 }}
                            allowClear
                            options={[
                                { value: undefined, label: 'Все пользователи' },
                                ...users.map((u) => ({
                                    value: u.fullName,
                                    label: u.fullName || u.username,
                                })),
                            ]}
                        />

                        <Button
                            icon={<ReloadOutlined />}
                            onClick={loadTasks}
                            loading={loading}
                        />
                    </Space>
                </div>

                <Table
                    columns={columns}
                    dataSource={filteredTasks}
                    rowKey={(r) => r.id}
                    loading={loading}
                    pagination={{
                        pageSize: 8,
                        position: ['bottomCenter'],
                        showSizeChanger: false,
                    }}
                    bordered
                    size="middle"
                    onRow={(record) => ({
                        onClick: () => openTaskModal(record.id),
                        style: { cursor: 'pointer', userSelect: 'none' },
                    })}
                    style={{
                        background: '#1f1f1f',
                        borderRadius: 12,
                        touchAction: 'manipulation',
                    }}
                />
            </Card>

            {/* Модалка деталей задачи */}
            <TaskDetailsModal
                taskId={selectedTaskId}
                open={isModalOpen}
                onClose={() => setIsModalOpen(false)}
            />
        </>
    )
}
