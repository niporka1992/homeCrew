import {
    Form,
    Input,
    DatePicker,
    Button,
    Select,
    Row,
    Col,
    Card,
    InputNumber,
    TimePicker,
    Alert,
    message,
} from 'antd'
import dayjs from 'dayjs'
import { useState } from 'react'
import { api } from '../../api/api.js'

export default function SimpleTaskForm({ onSuccess }) {
    const [form] = Form.useForm()
    const [unit, setUnit] = useState('seconds')
    const [showInterval, setShowInterval] = useState(false)
    const [timeError, setTimeError] = useState(null)
    const [loading, setLoading] = useState(false)

    // 🔹 обработка поля "кол-во повторов"
    const handleRepeatChange = (value) => {
        const count = Number(value) || 0
        setShowInterval(count > 0)
        if (count <= 0) form.setFieldValue('repeatInterval', null)
    }

    // 🔹 проверка времени старта
    const validateStartDateTime = (values) => {
        if (!values.startDate || !values.time) return false
        const datePart = dayjs(values.startDate).format('YYYY-MM-DD')
        const timePart = dayjs(values.time).format('HH:mm')
        const startDateTime = dayjs(`${datePart} ${timePart}`, 'YYYY-MM-DD HH:mm')
        return startDateTime.isBefore(dayjs())
    }

    // 🔹 обработчик сабмита
    const handleSubmit = async (values) => {
        setTimeError(null)
        setLoading(true)

        // ✅ Автоматически добавляем +1 минуту, если выбрано текущее время
        const now = dayjs()
        if (values.time && values.time.isSame(now, 'minute')) {
            values.time = now.add(1, 'minute')
        }

        if (validateStartDateTime(values)) {
            setTimeError('Время старта не может быть в прошлом')
            setLoading(false)
            return
        }

        try {
            const unitToMs = {
                seconds: 1000,
                minutes: 60 * 1000,
                hours: 60 * 60 * 1000,
                days: 24 * 60 * 60 * 1000,
            }

            const intervalValue = Number(values.repeatInterval) || 0
            const intervalMs = intervalValue * unitToMs[unit]

            const payload = {
                type: 'SIMPLE',
                jobName: values.jobName.trim(),
                startDate: values.startDate.format('YYYY-MM-DD'),
                time: values.time.format('HH:mm'),
                repeatCount: Number(values.repeatCount) || 0,
                repeatIntervalMs: intervalMs,
            }

            await api.post('/scheduler/job/simple', payload)

            message.success('Задача успешно создана')
            form.resetFields()
            setShowInterval(false)
            setTimeError(null)
            onSuccess?.()
        } catch (err) {
            console.error('Ошибка при создании задачи:', err)
            setTimeError(err.message || '❌ Ошибка при создании SIMPLE-задачи')
        } finally {
            setLoading(false)
        }
    }

    return (
        <Card
            title="Создание простой задачи"
            style={{
                maxWidth: 420,
                margin: '0 auto',
                borderRadius: 12,
                boxShadow: '0 4px 14px rgba(0,0,0,0.1)',
            }}
        >
            <Form
                layout="vertical"
                form={form}
                onFinish={handleSubmit}
                initialValues={{ repeatCount: 0 }}
            >
                <Form.Item
                    label="Имя задачи"
                    name="jobName"
                    rules={[{ required: true, message: 'Введите имя задачи' }]}
                >
                    <Input placeholder="Например: simpleTest" />
                </Form.Item>

                <Form.Item
                    label="Количество повторов"
                    name="repeatCount"
                    tooltip="0 = выполнить один раз"
                    rules={[{ type: 'number', min: 0, message: 'Количество должно быть ≥ 0' }]}
                >
                    <InputNumber
                        min={0}
                        style={{ width: '100%' }}
                        placeholder="Например: 3"
                        onChange={handleRepeatChange}
                    />
                </Form.Item>

                {showInterval && (
                    <Form.Item label="Интервал между запусками" required>
                        <Row gutter={8}>
                            <Col span={16}>
                                <Form.Item
                                    name="repeatInterval"
                                    noStyle
                                    rules={[
                                        { required: true, message: 'Введите интервал повторов' },
                                        { type: 'number', min: 1, message: 'Интервал должен быть > 0' },
                                    ]}
                                >
                                    <InputNumber
                                        min={1}
                                        style={{ width: '100%' }}
                                        placeholder="Введите число"
                                    />
                                </Form.Item>
                            </Col>
                            <Col span={8}>
                                <Select value={unit} onChange={setUnit}>
                                    <Select.Option value="seconds">сек</Select.Option>
                                    <Select.Option value="minutes">мин</Select.Option>
                                    <Select.Option value="hours">часы</Select.Option>
                                    <Select.Option value="days">дни</Select.Option>
                                </Select>
                            </Col>
                        </Row>
                    </Form.Item>
                )}

                <Row gutter={8}>
                    <Col span={12}>
                        <Form.Item
                            label="Дата старта"
                            name="startDate"
                            rules={[{ required: true, message: 'Выберите дату' }]}
                        >
                            <DatePicker style={{ width: '100%' }} format="DD.MM.YYYY" />
                        </Form.Item>
                    </Col>

                    <Col span={12}>
                        <Form.Item
                            label="Время старта"
                            name="time"
                            rules={[{ required: true, message: 'Выберите время' }]}
                        >
                            <TimePicker
                                style={{ width: '100%' }}
                                format="HH:mm"
                                onChange={(value) => {
                                    if (!value) return
                                    const now = dayjs()
                                    // если выбранное совпадает по минуте с текущим временем → добавляем +1 минуту
                                    if (value.isSame(now, 'minute')) {
                                        const plusOne = now.add(1, 'minute')
                                        form.setFieldValue('time', plusOne)
                                        message.info('⏰ Автоматически установлено +1 минута от текущего времени')
                                    }
                                }}
                            />
                        </Form.Item>

                    </Col>
                </Row>

                <div style={{ textAlign: 'center', marginTop: 16 }}>
                    <Button type="primary" htmlType="submit" loading={loading}>
                        Создать задачу
                    </Button>
                </div>

                {timeError && (
                    <div style={{ marginTop: 16 }}>
                        <Alert
                            message={timeError}
                            type="error"
                            showIcon
                            style={{ textAlign: 'left' }}
                        />
                    </div>
                )}
            </Form>
        </Card>
    )
}
