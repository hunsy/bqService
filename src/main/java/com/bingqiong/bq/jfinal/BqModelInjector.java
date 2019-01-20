package com.bingqiong.bq.jfinal;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.ActiveRecordException;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Table;
import com.jfinal.plugin.activerecord.TableMapping;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by hunsy on 2017/5/17.
 */
public class BqModelInjector {

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final <T> T inject(Class<?> modelClass, String modelName, HttpServletRequest request, boolean skipConvertError) {
        Object model = null;
        try {
            model = modelClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (model instanceof Model)
            injectActiveRecordModel((Model) model, modelName, request, skipConvertError);
        else
            injectCommonModel(model, modelName, request, modelClass, skipConvertError);

        return (T) model;
    }

    private static final void injectCommonModel(Object model, String modelName, HttpServletRequest request, Class<?> modelClass, boolean skipConvertError) {
        Method[] methods = modelClass.getMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            if (methodName.startsWith("set") == false)    // only setter method
                continue;

            Class<?>[] types = method.getParameterTypes();
            if (types.length != 1)                        // only one parameter
                continue;

            String attrName = methodName.substring(3);
            String value = request.getParameter(modelName + "." + StrKit.firstCharToLowerCase(attrName));
            if (value != null) {
                try {
                    method.invoke(model, TypeConverter.convert(types[0], value));
                } catch (Exception e) {
                    if (skipConvertError == false)
                        throw new RuntimeException(e);
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static final void injectActiveRecordModel(Model<?> model, String modelName, HttpServletRequest request, boolean skipConvertError) {
        Table table = TableMapping.me().getTable(model.getClass());

        String modelNameAndDot = StrKit.notBlank(modelName) ? modelName + "." : null;

        Map<String, String[]> parasMap = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : parasMap.entrySet()) {
            String paraName = entry.getKey();

            if (modelNameAndDot != null) {
                if (paraName.startsWith(modelNameAndDot)) {
                    paraName = paraName.substring(modelNameAndDot.length());
                } else {
                    continue;
                }
            }
            Class colType = table.getColumnType(paraName);
            if (colType == null)
                throw new ActiveRecordException("The model attribute " + paraName + " is not exists.");
            String[] paraValue = entry.getValue();
            try {
                // Object value = Converter.convert(colType, paraValue != null ? paraValue[0] : null);
                Object value = paraValue[0] != null ? TypeConverter.convert(colType, paraValue[0]) : null;
                model.set(paraName, value);
            } catch (Exception ex) {
                if (skipConvertError == false)
                    throw new RuntimeException("Can not convert parameter: " + modelNameAndDot + paraName, ex);
            }
        }
    }
}
