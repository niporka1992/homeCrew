import React from 'react'
import { Form, InputNumber, Alert } from 'antd'

export default function EveryNDaysMode() {
    return (
        <>
            <Alert
                message="Выберите интервал: задача будет выполняться каждые N дней с момента старта."
                type="info"
                showIcon
                style={{ marginBottom: 12 }}
            />
            <Form.Item
                label="Интервал (в днях)"
                name="dayOfMonth"
                rules={[{ required: true, message: 'Введите количество дней между выполнениями' }]}
            >
                <InputNumber min={1} max={365} style={{ width: '100%' }} placeholder="Например: 3" />
            </Form.Item>
        </>
    )
}
