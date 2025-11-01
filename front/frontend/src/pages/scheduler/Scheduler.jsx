import { useEffect, useState } from 'react'
import {
    Card,
    Typography,
    Table,
    Button,
    Space,
    Modal,
    Select,
    message,
    Tag,
    Tooltip,
} from 'antd'
import {
    PlusOutlined,
    PauseCircleOutlined,
    ReloadOutlined,
    PlayCircleOutlined,
    ThunderboltOutlined,
    ClockCircleOutlined,
} from '@ant-design/icons'
import CronTaskForm from './cron/CronTaskForm.jsx'
import SimpleTaskForm from './SimpleTaskForm'
import dayjs from 'dayjs'
import { api } from '../../api/api.js'

const { Title, Text } = Typography
const { Option } = Select

export default function Scheduler() {
    const [loading, setLoading] = useState(false)
    const [jobs, setJobs] = useState([])
    const [filter, setFilter] = useState('active')
    const [isCronModalOpen, setIsCronModalOpen] = useState(false)
    const [isSimpleModalOpen, setIsSimpleModalOpen] = useState(false)

    // 🔹 Загрузка задач
    const loadJobs = async () => {
        setLoading(true)
        try {
            const data = await api.get(`/scheduler/jobs?status=${filter}`)
            setJobs(data)
        } catch {
            message.error('Ошибка загрузки задач')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        loadJobs()
    }, [filter])

    // 🔹 Пауза / возобновление
    const handleToggleStatus = async (jobName, shouldActivate) => {
        try {
            await api.patch(
                `/scheduler/job/${encodeURIComponent(jobName)}/status?active=${shouldActivate}`
            )

            message.success(
                shouldActivate ? 'Задача возобновлена' : 'Задача приостановлена'
            )

            // обновляем таблицу после действия
            loadJobs()
        } catch (err) {
            message.error(err.message || 'Ошибка при изменении статуса')
        }
    }


    const columns = [
        {
            title: 'Имя задачи',
            dataIndex: 'name',
            key: 'name',
            render: (text) => (
                <Text strong style={{ color: '#69b1ff' }}>
                    {text}
                </Text>
            ),
        },
        {
            title: 'Описание',
            dataIndex: 'description',
            key: 'description',
            render: (v) => v || <Text type="secondary">—</Text>,
        },
        {
            title: 'Последний запуск',
            dataIndex: 'previousFireTime',
            key: 'previousFireTime',
            render: (v) => v ? dayjs(v).format('DD.MM.YYYY HH:mm') : '—',
        },
        {
            title: 'Следующий запуск',
            dataIndex: 'nextFireTime',
            key: 'nextFireTime',
            render: (v) => v ? dayjs(v).format('DD.MM.YYYY HH:mm') : '—',
        },
        {
            title: 'Статус',
            dataIndex: 'status',
            key: 'status',
            align: 'center',
            render: (status) => {
                const colorMap = {
                    NORMAL: 'green',
                    PAUSED: 'default',
                    BLOCKED: 'gold',
                    ERROR: 'red',
                    COMPLETE: 'purple',
                    NONE: 'gray',
                }
                const tooltipMap = {
                    NORMAL: 'Активна',
                    PAUSED: 'Приостановлена',
                    BLOCKED: 'Выполняется',
                    ERROR: 'Ошибка',
                    COMPLETE: 'Завершена',
                    NONE: 'Отсутствует в планировщике',
                }
                return (
                    <Tooltip title={tooltipMap[status]}>
                        <Tag color={colorMap[status] || 'default'}>{status}</Tag>
                    </Tooltip>
                )
            },
        },
        {
            title: 'Действие',
            key: 'action',
            align: 'center',
            render: (_, record) => {
                // если задача завершена — никаких действий
                if (record.status === 'COMPLETE') {
                    return <Text type="secondary">—</Text>
                }

                const isActive = record.status === 'NORMAL'
                const shouldActivate = !isActive
                const buttonLabel = shouldActivate ? 'Старт' : 'Пауза'

                return (
                    <Tooltip title={shouldActivate ? 'Возобновить' : 'Поставить на паузу'}>
                        <Button
                            icon={
                                shouldActivate ? (
                                    <PlayCircleOutlined style={{ color: '#52c41a' }} />
                                ) : (
                                    <PauseCircleOutlined style={{ color: '#ff4d4f' }} />
                                )
                            }
                            type={shouldActivate ? 'primary' : 'text'}
                            danger={!shouldActivate}
                            onClick={() => handleToggleStatus(record.name, shouldActivate)}
                        >
                            {buttonLabel}
                        </Button>
                    </Tooltip>
                )
            },
        }


    ]

    const handleCreateSuccess = () => {
        setIsCronModalOpen(false)
        setIsSimpleModalOpen(false)
        loadJobs()
    }

    return (
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
            {/* Заголовок */}
            <div
                style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginBottom: 16,
                }}
            >
                <Title level={3} style={{ color: '#e6f4ff', margin: 4 }}>
                    🕓 Планировщик
                </Title>

                <Space>
                    <Select
                        value={filter}
                        onChange={(val) => setFilter(val)}
                        style={{ width: 220 }}
                        options={[
                            { value: 'active', label: 'Активные' },
                            { value: 'paused', label: 'Остановленные' },
                            { value: 'other', label: 'Прочие' },
                        ]}
                    />
                    <Button
                        icon={<ReloadOutlined />}
                        onClick={loadJobs}
                        loading={loading}
                    />
                    <Button
                        type="default"
                        icon={<ClockCircleOutlined />}
                        onClick={() => setIsSimpleModalOpen(true)}
                    >
                        Простая задача
                    </Button>
                    <Button
                        type="primary"
                        icon={<ThunderboltOutlined />}
                        onClick={() => setIsCronModalOpen(true)}
                    >
                        CRON-задача
                    </Button>
                </Space>
            </div>

            {/* Таблица */}
            <Table
                columns={columns}
                dataSource={jobs}
                rowKey={(r) => r.name}
                loading={loading}
                pagination={{
                    pageSize: 8,
                    position: ['bottomCenter'],
                    showSizeChanger: false,
                }}
                bordered
                size="middle"
                style={{ background: '#1f1f1f', borderRadius: 12 }}
            />

            {/* SIMPLE модалка */}
            <Modal
                title="⚡ Простая задача"
                open={isSimpleModalOpen}
                onCancel={() => setIsSimpleModalOpen(false)}
                centered
                width={600}
                footer={null}
                destroyOnClose
            >
                <SimpleTaskForm onSuccess={handleCreateSuccess} />
            </Modal>

            {/* CRON модалка */}
            <Modal
                title="🧭 CRON-задача"
                open={isCronModalOpen}
                onCancel={() => setIsCronModalOpen(false)}
                centered
                width={600}
                footer={null}
                destroyOnClose
            >
                <CronTaskForm onSuccess={handleCreateSuccess} />
            </Modal>
        </Card>
    )
}
