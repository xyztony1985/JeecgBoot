<template>
        <a-range-picker
            v-model:value="rangeValue"
            @change="handleChange"
            :show-time="datetime"
            :placeholder="placeholder"
            :valueFormat="valueFormat"
            :presets="presets"
        />
</template>

<script>
    import { defineComponent, ref, watch, computed } from 'vue';
    import { propTypes } from '/@/utils/propTypes';
    import { Form } from 'ant-design-vue';
    import dayjs from 'dayjs';

    const placeholder = ['开始日期', '结束日期']
    /**
     * 用于范围查询
     */
    export default defineComponent({
        name: "JRangeDate",
        props:{
            value: propTypes.string.def(''),
            datetime: propTypes.bool.def(false),
            placeholder: propTypes.string.def(''),
        },
        emits:['change', 'update:value'],
        setup(props, {emit}){
            const rangeValue = ref([])
            const formItemContext = Form.useInjectFormItemContext();

            const valueFormat = computed(()=>{
                if(props.datetime === true){
                    return 'YYYY-MM-DD HH:mm:ss'
                }else{
                    return 'YYYY-MM-DD'
                }
            });

            watch(()=>props.value, (val)=>{
                const fmt = valueFormat.value;
                if(val){
                    const parts = val.split(',');
                    // convert incoming comma-separated strings to dayjs instances (or null)
                    rangeValue.value = parts.map((p) => (p ? dayjs(p, fmt) : null));
                }else{
                    rangeValue.value = []
                }
            }, {immediate: true});

            const presets = computed(()=>{
                const fmt = valueFormat.value;
                const today = dayjs();
                const startOfDay = (d) => d.startOf('day').format(fmt);
                const endOfDay = (d) => d.endOf('day').format(fmt);

                return [
                    { label: '今日', value: [today.startOf('day'), today.endOf('day')] },
                    { label: '本周', value: [dayjs().startOf('week'), dayjs().endOf('week')] },
                    { label: '上周', value: [dayjs().subtract(1, 'week').startOf('week'), dayjs().subtract(1, 'week').endOf('week')] },
                    { label: '本月', value: [dayjs().startOf('month'), dayjs().endOf('month')] },
                    { label: '上个月', value: [dayjs().subtract(1, 'month').startOf('month'), dayjs().subtract(1, 'month').endOf('month')] },
                    { label: '下个月', value: [dayjs().add(1, 'month').startOf('month'), dayjs().add(1, 'month').endOf('month')] },
                    { label: '本年度', value: [dayjs().startOf('year'), dayjs().endOf('year')] },
                    { label: '上年度', value: [dayjs().subtract(1, 'year').startOf('year'), dayjs().subtract(1, 'year').endOf('year')] },
                ];
            });

                        function handleChange(arr){
                                let str = ''
                                if(arr && arr.length>0){
                                    // 代码逻辑说明: [issues/6368] rangeDate去掉判断允许起始项或结束项为空兼容allowEmpty
                                    const fmt = valueFormat.value;
                                    const parts = arr.map((it) => {
                                        if(!it) return '';
                                        // if it's a dayjs/moment-like object
                                        if(typeof it === 'object' && typeof it.format === 'function'){
                                            return it.format(fmt);
                                        }
                                        // fallback to string
                                        return String(it);
                                    });
                                    str = parts.join(',');
                                }
                                emit('change', str);
                                emit('update:value', str);
                                formItemContext.onFieldChange();
                        }
            return {
                rangeValue,
                placeholder,
                valueFormat,
                handleChange,
                presets
            }
        }
    });
</script>

<style scoped>

</style>
