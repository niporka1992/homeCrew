import React from 'react'
import { Form, Select, Alert } from 'antd'

export default function YearlyMode() {
    const months = [
        'Январь','Февраль','Март','Апрель','Май','Июнь',
        'Июль','Август','Сентябрь','Октябрь','Ноябрь','Декабрь',
    ].map((m, i) => ({ label: m, value: i + 1 }))

    return (
        <>
            <Alert
                message="Выберите месяцы, когда задача должна выполняться (например, в январе и июле)."
                type="info"
                showIcon
                style={{ marginBottom: 12 }}
            />
            <Form.Item
                label="Месяцы"
                name="months"
                rules={[{ required: true, message: 'Выберите хотя бы один месяц' }]}
            >
                <Select
                    mode="multiple"
                    placeholder="Например: Январь, Июль"
                    options={months}
                    style={{ width: '100%' }}
                />
            </Form.Item>
        </>
    )
}
