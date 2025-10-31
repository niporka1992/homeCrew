import { useEffect, useState } from 'react'
import {
    Modal,
    Descriptions,
    Divider,
    Spin,
    Tag,
    Empty,
    Timeline,
    List,
    Typography,
    message,
    Image,
    Space,
} from 'antd'
import dayjs from 'dayjs'
import {
    FileOutlined,
    MessageOutlined,
    ClockCircleOutlined,
    UserOutlined,
} from '@ant-design/icons'
import { api } from '../../api/api.js'

const { Text } = Typography

export default function TaskDetailsModal({ taskId, open, onClose }) {
    const [task, setTask] = useState(null)
    const [loading, setLoading] = useState(false)
    const [previews, setPreviews] = useState({}) // ✅ blob-URL для превью

    // ==========================
    // 🔁 Загрузка деталей задачи
    // ==========================
    useEffect(() => {
        if (!open || !taskId) return

        const loadTask = async () => {
            setLoading(true)
            try {
                const data = await api.get(`/tasks/${taskId}/details`)
                setTask(data)
            } catch (err) {
                console.error('Ошибка при загрузке деталей задачи:', err)
                message.error(err.message || 'Не удалось загрузить детали задачи')
            } finally {
                setLoading(false)
            }
        }

        loadTask()
    }, [taskId, open])

    // ==========================
    // 🖼️ Подгрузка превьюшек
    // ==========================
    useEffect(() => {
        if (!task?.history) return

        const loadPreviews = async () => {
            const map = {}
            for (const h of task.history) {
                for (const a of h.attachments || []) {
                    try {
                        const blob = await api.blob(`/files/${encodeURIComponent(a.fileUrl)}`)
                        map[a.fileUrl] = URL.createObjectURL(blob)
                    } catch (err) {
                        console.warn('Не удалось загрузить превью:', a.fileUrl, err)
                    }
                }
            }
            setPreviews(map)
        }

        loadPreviews()

        // 🧹 Очистка blob-URL при закрытии
        return () => {
            Object.values(previews).forEach((url) => URL.revokeObjectURL(url))
        }
    }, [task])

    // ==========================
    // 🏷️ Цвета статусов
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
        return <Tag color={colorMap[status] || 'default'}>{labelMap[status] || status}</Tag>
    }

    // ==========================
    // 📥 Загрузка файла
    // ==========================

    const handleDownload = async (filePath, fileName) => {
        try {
            const blob = await api.blob(`/files/${encodeURIComponent(filePath)}`)
            const url = URL.createObjectURL(blob)
            const a = document.createElement('a')

            // если имя не указано, берём из пути
            let name = fileName || filePath.split('/').pop() || 'file'

            // добавляем .png если нет расширения
            if (!name.toLowerCase().endsWith('.png')) {
                name += '.png'
            }

            a.href = url
            a.download = name
            a.click()
            URL.revokeObjectURL(url)
        } catch (err) {
            console.error(err)
            message.error('Ошибка при скачивании файла')
        }
    }


    // ==========================
// 📎 Отображение вложений
// ==========================
    const renderAttachments = (attachments) => {
        if (!attachments || attachments.length === 0) return null

        return (
            <List
                dataSource={attachments}
                renderItem={(a) => {
                    const hasPreview = Boolean(previews[a.fileUrl])

                    return (
                        <List.Item>
                            <Space align="center" size="large">
                                {/* 🔹 Превью без скачивания при клике */}
                                {hasPreview ? (
                                    <Image
                                        src={previews[a.fileUrl]}
                                        alt={a.fileName || 'attachment'}
                                        width={200}
                                        style={{ borderRadius: 8 }}
                                        preview={{ mask: null }} // не вызывает скачивание
                                        fallback="/no-preview.png"
                                    />
                                ) : (
                                    <FileOutlined
                                        style={{ fontSize: 48, color: '#1677ff', opacity: 0.6 }}
                                    />
                                )}

                                {/* 🔹 Кнопка "Скачать" */}
                                <Text
                                    style={{
                                        color: '#1677ff',
                                        cursor: 'pointer',
                                        textDecoration: 'underline',
                                    }}
                                    onClick={() => handleDownload(a.fileUrl, a.fileName)}
                                >
                                    Скачать файл
                                </Text>
                            </Space>
                        </List.Item>
                    )
                }}
            />
        )
    }


    // ==========================
    // 🧩 Основной UI
    // ==========================
    return (
        <Modal
            title="📄 Детали задачи"
            open={open}
            onCancel={onClose}
            footer={null}
            width={860}
            centered
            destroyOnClose
        >
            {loading ? (
                <div style={{ display: 'flex', justifyContent: 'center', padding: 40 }}>
                    <Spin size="large" />
                </div>
            ) : !task ? (
                <Empty description="Нет данных" />
            ) : (
                <>
                    {/* === Основная информация === */}
                    <Descriptions bordered column={1} size="middle">
                        <Descriptions.Item label="Описание">
                            {task.description || <Text type="secondary">—</Text>}
                        </Descriptions.Item>
                        <Descriptions.Item label="Исполнитель">
                            {task.assigneeFullName || <Text type="secondary">Не назначен</Text>}
                        </Descriptions.Item>
                        <Descriptions.Item label="Статус">
                            {statusTag(task.status)}
                        </Descriptions.Item>
                        <Descriptions.Item label="Создана">
                            {dayjs(task.dateOfCreate).format('DD.MM.YYYY HH:mm')}
                        </Descriptions.Item>
                    </Descriptions>

                    {/* === История === */}
                    <Divider>История изменений</Divider>
                    {task.history && task.history.length > 0 ? (
                        <Timeline
                            mode="left"
                            items={task.history.map((h) => ({
                                dot: <ClockCircleOutlined style={{ color: '#1677ff' }} />,
                                color: 'blue',
                                children: (
                                    <div style={{ marginBottom: 16 }}>
                                        <b>{h.details}</b>

                                        {/* 👇 Показываем статус, если он есть */}
                                        {h.statusAfter && (
                                            <div style={{ marginTop: 4 }}>
                                                <Text type="secondary">→ </Text>
                                                {statusTag(h.statusAfter)}
                                            </div>
                                        )}

                                        <div style={{ marginTop: 4 }}>
                                            <UserOutlined />{' '}
                                            {h.actorFullName || (
                                                <Text type="secondary">Система</Text>
                                            )}
                                        </div>

                                        <Text type="secondary">
                                            {dayjs(h.createdAt).format('DD.MM.YYYY HH:mm')}
                                        </Text>

                                        {/* Комментарии */}
                                        {h.comments && h.comments.length > 0 && (
                                            <List
                                                size="small"
                                                dataSource={h.comments}
                                                renderItem={(c) => (
                                                    <List.Item>
                                                        <MessageOutlined
                                                            style={{
                                                                fontSize: 16,
                                                                color: '#52c41a',
                                                                marginRight: 6,
                                                            }}
                                                        />
                                                        <Text strong>{c.authorName}:</Text>{' '}
                                                        <Text>{c.text}</Text>
                                                    </List.Item>
                                                )}
                                            />
                                        )}

                                        {/* Вложения */}
                                        {renderAttachments(h.attachments)}
                                    </div>
                                ),
                            }))}
                        />
                    ) : (
                        <Empty description="История пока отсутствует" />
                    )}
                </>
            )}
        </Modal>
    )
}
