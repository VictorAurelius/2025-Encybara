export const formatScore = (value: number | null | undefined, decimals: number = 2) => {
    if (value === null || value === undefined) return '0.00';
    return value.toFixed(decimals);
};