import { useState } from 'react'
import {
    Form,
    Input,
    Select,
    DatePicker,
    TimePicker,
    Button,
    Divider,
    Row,
    Col,
    Alert,
    message,
} from 'antd'
import dayjs from 'dayjs'
import 'dayjs/locale/ru'
import { api } from '../../../api/api.js'

import DailyMode from './CronTaskForm/DailyMode.jsx'
import WeeklyMode from './CronTaskForm/WeeklyMode.jsx'
import MonthlyMode from './CronTaskForm/MonthlyMode.jsx'
import YearlyMode from './CronTaskForm/YearlyMode.jsx'
import CustomCronMode from './CronTaskForm/CustomCronMode.jsx'

const { Option } = Select

export default function CronTaskForm({ onSuccess }) {
    const [form] = Form.useForm()
    const [mode, setMode] = useState('DAILY')
    const [loading, setLoading] = useState(false)
    const [timeError, setTimeError] = useState(null)

    // === Проверка дат ===
    const validateDateTime = (startDate, startTime, endDate, endTime) => {
        if (!startDate || !startTime) return null

        const start = dayjs(`${startDate.format('YYYY-MM-DD')} ${startTime.format('HH:mm')}`)
        const now = dayjs()

        if (start.isBefore(now)) return '⛔ Время начала не может быть в прошлом'

        if (endDate && endTime) {
            const end = dayjs(`${endDate.format('YYYY-MM-DD')} ${endTime.format('HH:mm')}`)
            if (end.isBefore(start)) return '⛔ Время окончания не может быть раньше времени начала'
        }

        return null
    }

    // === Сборка payload ===
    const buildPayload = (values) => {
        const base = {
            type: mode,
            jobName: values.jobName.trim(),
            startDate: values.startDate?.format('YYYY-MM-DD'),
            startTime: values.startTime?.format('HH:mm'),
            endDate: values.endDate ? values.endDate.format('YYYY-MM-DD') : null,
            endTime: values.endTime ? values.endTime.format('HH:mm') : null,
        }

        switch (mode) {
            case 'DAILY': return base
            case 'WEEKLY': return { ...base, daysOfWeek: values.daysOfWeek }
            case 'MONTHLY': return { ...base, daysOfMonth: values.daysOfMonth }
            case 'YEARLY': return { ...base, months: values.months }
            case 'CUSTOM': return { ...base, customCron: values.customCron }
            default: throw new Error(`Неизвестный режим: ${mode}`)
        }
    }

    // === Сабмит ===
    const handleSubmit = async (values) => {
        const validationError = validateDateTime(
            values.startDate,
            values.startTime,
            values.endDate,
            values.endTime
        )

        if (validationError) {
            setTimeError(validationError)
            return
        }

        try {
            setLoading(true)
            setTimeError(null)

            const payload = buildPayload(values)

            // ✅ используем api.post с автоматическим токеном
            await api.post('/scheduler/job/cron', payload)

            message.success('CRON-задача успешно создана')
            form.resetFields()
            form.setFieldsValue({
                mode: 'DAILY',
                startDate: dayjs(),
                startTime: dayjs(),
            })
            setMode('DAILY')
            setTimeError(null)
            onSuccess?.()
        } catch (err) {
            console.error('Ошибка при создании CRON-задачи:', err)
            message.error(err.message || '❌ Ошибка при создании CRON-задачи')
        } finally {
            setLoading(false)
        }
    }

    const handleDateOrTimeChange = () => {
        if (timeError) setTimeError(null)
    }

    const renderModeForm = () => {
        switch (mode) {
            case 'DAILY': return <DailyMode />
            case 'WEEKLY': return <WeeklyMode />
            case 'MONTHLY': return <MonthlyMode />
            case 'YEARLY': return <YearlyMode />
            case 'CUSTOM': return <CustomCronMode />
            default: return null
        }
    }

    return (
        <Form
            layout="vertical"
            form={form}
            onFinish={handleSubmit}
            style={{ marginTop: 8 }}
            initialValues={{
                mode: 'DAILY',
                startDate: dayjs(),
                startTime: dayjs(),
            }}
        >
            <Form.Item
                label="Имя задачи"
                name="jobName"
                rules={[{ required: true, message: 'Введите имя задачи' }]}
            >
                <Input placeholder="Например: проверка отчётов" />
            </Form.Item>

            <Form.Item
                label="Тип расписания"
                name="mode"
                rules={[{ required: true, message: 'Выберите тип расписания' }]}
            >
                <Select onChange={setMode}>
                    <Option value="DAILY">Ежедневно</Option>
                    <Option value="WEEKLY">Еженедельно</Option>
                    <Option value="MONTHLY">Ежемесячно</Option>
                    <Option value="YEARLY">Ежегодно</Option>
                    <Option value="CUSTOM">Произвольное (CRON)</Option>
                </Select>
            </Form.Item>

            {renderModeForm()}

            <Divider style={{ margin: '12px 0' }} />

            {mode !== 'CUSTOM' && (
                <>
                    <Row gutter={12}>
                        <Col span={12}>
                            <Form.Item
                                label="Дата начала"
                                name="startDate"
                                rules={[{ required: true, message: 'Выберите дату начала' }]}
                            >
                                <DatePicker
                                    format="DD.MM.YYYY"
                                    style={{ width: '100%' }}
                                    onChange={handleDateOrTimeChange}
                                />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item
                                label="Время начала"
                                name="startTime"
                                rules={[{ required: true, message: 'Выберите время начала' }]}
                            >
                                <TimePicker
                                    format="HH:mm"
                                    style={{ width: '100%' }}
                                    onChange={handleDateOrTimeChange}
                                />
                            </Form.Item>
                        </Col>
                    </Row>

                    <Row gutter={12}>
                        <Col span={12}>
                            <Form.Item label="Дата окончания" name="endDate">
                                <DatePicker
                                    format="DD.MM.YYYY"
                                    style={{ width: '100%' }}
                                    onChange={handleDateOrTimeChange}
                                />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item label="Время окончания" name="endTime">
                                <TimePicker
                                    format="HH:mm"
                                    style={{ width: '100%' }}
                                    onChange={handleDateOrTimeChange}
                                />
                            </Form.Item>
                        </Col>
                    </Row>
                </>
            )}

            <Button type="primary" htmlType="submit" loading={loading} block>
                Создать задачу
            </Button>

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
    )
}
