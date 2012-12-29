
package com.explorer.ftp;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.explorer.R;
import com.explorer.activity.FTPActivity;
import com.explorer.common.FileUtil;

/**data adapter to provide data for the list of thumbnail
 *CNcomment: 数据适配器为列表或者缩略图提供数据
 * 
 * @author qian_wei
 */
public class FtpFileAdapter extends BaseAdapter {
    // folder
    private Bitmap folder_File;

    // audio file
    private Bitmap music_File_mp3;

    // other file
    private Bitmap other_File;

    // video file
    private Bitmap vedio_File;

    // APK file
    private Bitmap apk_File;

    // picturn file
    private Bitmap img_File;

    // multiple-choice path to the file collection
    //CNcomment: 多选时文件的路径集合
    private List<String> selectList = null;

    // file name list
    private List<String> nameList;

    // data container layout
    private int layout = 0;

    // file name
    private String name = null;

    // the type of file
    private String type = null;

    // resolve the layout resource file
    //CNcomment: 解析布局资源文件
    LayoutInflater inflater;

    // page associated 
    //CNcomment: 关联页面
    FTPActivity context;

    /**the path to the file when multiple choice set
     *CNcomment: 获得多选时文件的路径集合
     * 
     * @return  path to the file collection
     */
    public List<String> getSelectList() {
        return selectList;
    }

    /**constructor ,the ininalization data
     *CNcomment: 构造方法，初始化数据
     * 
     * @param context 
     * @param list 
     * @param fileString 
     * @param layout 
     */
    public FtpFileAdapter(FTPActivity context, List<String> nameList, int layout) {
        // ininalization parameters
        this.nameList = nameList;
        this.layout = layout;
        selectList = new ArrayList<String>();
        inflater = LayoutInflater.from(context);
        this.context = context;
        // According to the different file types together with the different picture
	//CNcomment: 根据不同的文件类型配以不同的图片显示
        other_File = BitmapFactory.decodeResource(context.getResources(), R.drawable.otherfile);
        apk_File = BitmapFactory.decodeResource(context.getResources(), R.drawable.list);
        vedio_File = BitmapFactory.decodeResource(context.getResources(), R.drawable.vediofile);
        music_File_mp3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.mp3file);
        folder_File = BitmapFactory.decodeResource(context.getResources(), R.drawable.folder_file);
        img_File = BitmapFactory.decodeResource(context.getResources(), R.drawable.imgfile);
    }

    /**the number of data containers
     *CNcomment: 获得容器中数据的数目
     */

    public int getCount() {
        return nameList.size();
    }

    /**for each option object container
     *CNcomment: 获得容器中每个选项对象
     */

    public Object getItem(int position) {
        return nameList.get(position);
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
        /**according to the view whether there is to ininialize the controls in each data container
         *CNcomment: 根据视图是否存在 初始化每个数据容器中的控件
         */
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(layout, null);
            holder.text = (TextView) convertView.findViewById(R.id.text);
            holder.icon = (ImageView) convertView.findViewById(R.id.image_Icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (nameList.size() > 0) {
            type = nameList.get(position).split("\\|")[0];
            name = nameList.get(position).split("\\|")[1].trim();
            if (name.contains("\n")) {
                name = name.substring(0, name.indexOf("\n"))
                        + name.substring(name.indexOf("\n") + 1, name.length());
            }
            String f_type = FileUtil.getMIMEType(name, context);
            // get the type of file
            if (type.equals("d")) {
                holder.icon.setImageBitmap(folder_File);
            } else if ("audio/*".equals(f_type)) {
                holder.icon.setImageBitmap(music_File_mp3);
            } else if ("video/*".equals(f_type)) {
                holder.icon.setImageBitmap(vedio_File);
            } else if ("apk/*".equals(f_type)) {
                holder.icon.setImageBitmap(apk_File);
            } else if ("image/*".equals(f_type)) {
                holder.icon.setImageBitmap(img_File);
            } else {
                holder.icon.setImageBitmap(other_File);
            }

            if (layout == R.layout.gridfile_row) {
                if (holder.text.getPaint().measureText(name) > holder.text.getLayoutParams().width) {
                    holder.text.setEllipsize(TruncateAt.MARQUEE);
                    holder.text.setMarqueeRepeatLimit(android.R.attr.marqueeRepeatLimit);
                    holder.text.setHorizontallyScrolling(true);
                } else {
                    holder.text.setGravity(Gravity.CENTER_HORIZONTAL);
                }
            }

            holder.text.setText(name);
        }
        return convertView;
    }

    /**control container class storage control
     *CNcomment: 控件容器类存放控件
     * 
     * @author qian_wei
     */
    private static class ViewHolder {
        private TextView text;

        private ImageView icon;
    }

}
