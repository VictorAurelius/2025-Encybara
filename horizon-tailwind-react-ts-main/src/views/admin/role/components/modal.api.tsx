import { Card, Col, Collapse, Row, Tooltip } from 'antd';
import { ProFormSwitch } from '@ant-design/pro-components';
import { grey } from '@ant-design/colors';
import { colorMethod, groupByPermission } from '../../permission/components/color.method';
import { IRole } from './modal.role';
import { IPermission } from '../../permission/components/modal.permission';
import type { ProFormInstance } from '@ant-design/pro-components';
import { useEffect, useMemo } from 'react';
import type { CollapseProps } from 'antd';


interface IProps {
    onChange?: (data: any[]) => void;
    onReset?: () => void;
    form: ProFormInstance;
    listPermissions: {
        module: string;
        permissions: IPermission[]
    }[] | null;

    singleRole: IRole | null;
    openModal: boolean;
};

const ModuleApi = (props: IProps) => {
    const { form, listPermissions, singleRole, openModal } = props;

    useEffect(() => {

        if (listPermissions?.length && singleRole?.id && openModal === true) {

            //current permissions of role
            const userPermissions = groupByPermission(
                Array.isArray(singleRole.permissions) && typeof singleRole.permissions[0] === 'object'
                    ? singleRole.permissions as IPermission[]
                    : []
            );


            let p: any = {};

            listPermissions.forEach(x => {
                let allCheck = true;
                x.permissions?.forEach(y => {
                    const temp = userPermissions.find(z => z.module === x.module);

                    p[y.id!] = false;

                    if (temp) {
                        const isExist = temp.permissions.find(k => k.id === y.id);
                        if (isExist) {
                            // form.setFieldValue(["permissions", y.id as string], true);
                            p[y.id!] = true;
                        } else allCheck = false;
                    } else {
                        allCheck = false;
                    }
                })


                // form.setFieldValue(["permissions", x.module], allCheck);
                p[x.module] = allCheck;

            })



            form.setFieldsValue({
                name: singleRole.name,
                active: singleRole.active,
                description: singleRole.description,
                permissions: p
            })


        }
    }, [openModal, listPermissions, singleRole, form]);

    const handleSwitchAll = (value: boolean, name: string) => {
        console.log(`Switch all for module ${name}:`, value);
        const child = listPermissions?.find(item => item.module === name);
        if (child) {
            child?.permissions?.forEach(item => {
                if (item.id)
                    form.setFieldValue(["permissions", item.id], value)
            })
        }
    }

    const handleSingleCheck = (value: boolean, child: number, parent: string) => {
        console.log(`Switch single permission ${child} in module ${parent}:`, value);
        form.setFieldValue(["permissions", child], value);

        //check all
        const temp = listPermissions?.find(item => item.module === parent);
        if (temp?.module) {
            const restPermission = temp?.permissions?.filter(item => item.id !== child);
            if (restPermission && restPermission.length) {
                const allTrue = restPermission.every(item => form.getFieldValue(["permissions", item.id]));
                form.setFieldValue(["permissions", parent], allTrue && value)
            }
        }

    }

    const panels = useMemo(() => {
        const uniqueModules = new Set();
        return listPermissions?.filter(item => {
            if (uniqueModules.has(item.module)) {
                return false;
            }
            uniqueModules.add(item.module);
            return true;
        }).map((item, index) => {

            return {
                key: `${item.module}-${index}`, // Đảm bảo key là duy nhất
                label: <div>{item.module}</div>,
                forceRender: true,
                extra: (
                    <div className="customize-form-item">
                        <ProFormSwitch
                            name={["permissions", item.module]}
                            fieldProps={{
                                defaultChecked: false,
                                onClick: (u, e) => { e.stopPropagation(); },
                                onChange: (value) => handleSwitchAll(value, item.module),
                            }}
                        />
                    </div>
                ),
                children: (
                    <Row gutter={[16, 16]}>
                        {item.permissions?.map((value, i: number) => (
                            <Col lg={12} md={12} sm={24} key={`${value.id}-${i}`}>
                                <Card size="small" bodyStyle={{ display: "flex", flex: 1, flexDirection: 'row' }}>
                                    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                                        <ProFormSwitch
                                            name={["permissions", value.id]}
                                            fieldProps={{
                                                defaultChecked: false,
                                                onChange: (v) => handleSingleCheck(v, (value.id), item.module)
                                            }}
                                        />
                                    </div>
                                    <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                                        <Tooltip title={value?.name}>
                                            <p style={{ paddingLeft: 10, marginBottom: 3 }}>{value?.name || ''}</p>
                                            <div style={{ display: 'flex' }}>
                                                <p style={{ paddingLeft: 10, fontWeight: 'bold', marginBottom: 0, color: colorMethod(value?.method as string) }}>{value?.method || ''}</p>
                                                <p style={{ paddingLeft: 10, marginBottom: 0, color: grey[5] }}>{value?.apiPath || ''}</p>
                                            </div>
                                        </Tooltip>
                                    </div>
                                </Card>
                            </Col>
                        ))}
                    </Row>
                )
            };
        });
    }, [listPermissions]);

    return (
        <Card size="small" bordered={false}>
            <Collapse items={panels} />
        </Card>
    );
};

export default ModuleApi;
