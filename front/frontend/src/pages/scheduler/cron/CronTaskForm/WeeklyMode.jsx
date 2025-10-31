import React from 'react'
import { Form, Select, Alert } from 'antd'

const { Option } = Select

export default function WeeklyMode() {
    return (
        <>
            <Alert
                message="Выберите дни недели, когда должна выполняться задача."
                type="info"
                showIcon
                style={{ marginBottom: 12 }}
            />
            <Form.Item
                label="Дни недели"
                name="daysOfWeek"
                rules={[{ required: true, message: 'Выберите хотя бы один день недели' }]}
            >
                <Select
                    mode="multiple"
                    placeholder="Например: Пн, Ср, Пт"
                    style={{ width: '100%' }}
                >
                    <Option value="MON">Понедельник</Option>
                    <Option value="TUE">Вторник</Option>
                    <Option value="WED">Среда</Option>
                    <Option value="THU">Четверг</Option>
                    <Option value="FRI">Пятница</Option>
                    <Option value="SAT">Суббота</Option>
                    <Option value="SUN">Воскресенье</Option>
                </Select>
            </Form.Item>
        </>
    )
}
