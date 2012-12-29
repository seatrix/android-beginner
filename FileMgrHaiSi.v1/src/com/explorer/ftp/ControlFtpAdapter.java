
package com.explorer.ftp;

import java.util.List;
import com.explorer.activity.FTPActivity;
import com.explorer.common.FileUtil;
import com.explorer.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

/**Data adapter to provide data for the list or thumbnail
 *CNcomment: 数据适配器 为列表或者缩略图提供数据
 * 
 * @author qian_wei
 */
public class ControlFtpAdapter extends BaseAdapter {

    // Multiple-choice path to the file collection
    //CNcomment: 多选时文件的路径集合
    private List<String> selectList = null;

    // data collection layout
    private int layout = 0;

    // file name 
    private String name = null;

    // type of file
    private String type = null;

    //Resolve the layout resource file
    //CNcomment: 解析布局资源文件
    LayoutInflater inflater;

    FTPActivity context;

    /**
     * Constructor
     * 
     * @param context 
     * @param list 
     * @param fileString 
     * @param layout
     */
    public ControlFtpAdapter(FTPActivity context, List<String> nameList, int layout) {

        // initialization parameters
        this.layout = layout;
        selectList = nameList;
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    /**the number of data containers
     *CNcomment: 获得容器中数据的数目
     */

    public int getCount() {
        return selectList.size();
    }

    /**for each option object container
     *CNcomment: 获得容器中每个选项对象
     */

    public Object getItem(int position) {
        return selectList.get(position);
    }

    /**Access to each option in the container object
     *CNcomment: 获得容器中每个选项对象的ID
     */

    public long getItemId(int position) {
        return position;
    }

    /**assignment for each option object
     *CNcomment: 为每个选项对象赋值
     */

    public View getView(final int position, View convertView, ViewGroup parent) {
       
        final ViewHolder holder;
        /**According to the view whether there is to initialize the controls in each data container
         *CNcomment: 根据视图是否存在 初始化每个数据容器中的控件
         */
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(layout, null);
            holder.checkedTxt = (CheckedTextView) convertView.findViewById(R.id.check);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (selectList.size() > 0) {
            type = selectList.get(position).split("\\|")[0];
            name = selectList.get(position).split("\\|")[1].trim();
            // remove \n from file name
            if (name.contains("\n")) {
                name = name.substring(0, name.indexOf("\n"))
                        + name.substring(name.indexOf("\n") + 1, name.length());
            }
	    //To file MINIType to fill the different images
            //CNcomment: 获得文件的MINIType，填充不同的图像
            String f_type = FileUtil.getMIMEType(name, context);
            if (type.equals("d")) {
                holder.checkedTxt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.folder_file,
                        0, 0, 0);
            } else if ("audio/*".equals(f_type)) {
                holder.checkedTxt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.mp3file, 0, 0,
                        0);
            } else if ("video/*".equals(f_type)) {
                holder.checkedTxt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.vediofile, 0,
                        0, 0);
            } else if ("apk/*".equals(f_type)) {
                holder.checkedTxt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.list, 0, 0, 0);
            } else if ("image/*".equals(f_type)) {
                holder.checkedTxt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.imgfile, 0, 0,
                        0);
            } else {
                holder.checkedTxt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.otherfile, 0,
                        0, 0);
            }

	    //Triggered when the character length of more than 66
            //CNcomment: 字符长度超过66时触发
            if (name.length() > 66) {
                holder.checkedTxt.setText(name.substring(0, 55) + "...");
            } else {
                holder.checkedTxt.setText(name);
            }
        }
        return convertView;
    }

    /**Control container class
     *CNcomment: 控件容器类
     * 
     * @author qian_wei 
     */
    private static class ViewHolder {
        private CheckedTextView checkedTxt;
    }

}
