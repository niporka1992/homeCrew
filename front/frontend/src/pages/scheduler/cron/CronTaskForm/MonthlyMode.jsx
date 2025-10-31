import React from 'react'
import { Form, Select, Alert } from 'antd'

export default function MonthlyMode() {
    const days = Array.from({ length: 31 }, (_, i) => ({
        value: i + 1,
        label: `${i + 1}`,
    }))

    return (
        <>
            <Alert
                message="Выберите дни месяца, когда должна выполняться задача."
                type="info"
                showIcon
                style={{ marginBottom: 12 }}
            />
            <Form.Item
                label="Дни месяца"
                name="daysOfMonth"
                rules={[{ required: true, message: 'Выберите хотя бы один день месяца' }]}
            >
                <Select
                    mode="multiple"
                    placeholder="Например: 1, 15, 30"
                    style={{ width: '100%' }}
                    options={days}
                />
            </Form.Item>
        </>
    )
}
