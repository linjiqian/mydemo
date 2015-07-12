package cn.linjiqian.downuprefreshlistview_library;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author jiqian
 * @创建时间 2015-7-9 上午11:05:19
 * @描述 下拉刷新ListView
 * 
 * @svn 提交者: $Author: super $
 * @提交时间 $Date: 2015-07-12 21:09:37 +0800 (Sun, 12 Jul 2015) $
 * @当前版本 $Rev: 11 $
 * 
 */
public class DownAndUpRefreshListView extends ListView {

	private LinearLayout head; // 头部
	private View tail; // 尾部
	private ImageView arrow; // 剪头
	private TextView des; // 提示
	private TextView time; // 上次刷新时间
	private RelativeLayout rl; // 头部整个布局
	private int headHeight; // 头部高度
	private int lv_Screen_Y = 0; // liastView在屏幕上的位置
	private View lunBoTuPager;
	private ProgressBar pb; // 进度条
	private OnCallBackRefreshListener listener; // 回调的接口
	private boolean isOffRefresh = true; // 下拉刷新是否开启
	private boolean isLoadingMor = false; // 上拉加载更多数据

	float downY = -1;

	private final int HEAD_STATE_DOWN_REFRESH = 1; // 下拉刷新状态
	private final int HEAD_STATE_UP_REFRESH = 2; // 释放刷新
	private final int HEAD_STATE_YES_REFRESH = 3; // 正在刷新
	private int headState = HEAD_STATE_DOWN_REFRESH; // 默认状态
	private RotateAnimation rotate_up; // 从下拉刷新到释放刷新动画
	private RotateAnimation rotate_down; //
	private int tailHeight;

	public DownAndUpRefreshListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		initView();
		initEvent();
		initRotateAnimation(); // 初始化动画
	}

	/**
	 * 事件
	 */
	private void initEvent() {
		// 尾部加载数据
		this.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (getLastVisiblePosition() == getAdapter().getCount() - 1
						&& !isLoadingMor) {
					// 显示最后一个
					tail.setPadding(0, 0, 0, 0);
					setSelection(getAdapter().getCount());

					isLoadingMor = true;
					// 如果回调
					if (listener != null) {
						listener.loadingMor();
					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

			}
		});
	}

	public DownAndUpRefreshListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DownAndUpRefreshListView(Context context) {
		this(context, null);
	}

	private void initView() {
		initTail(); // 先加尾部,再加头部
		initHead();
	}

	// 下拉刷新事件
	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downY = ev.getY(); // 按下的座標
			break;
		case MotionEvent.ACTION_MOVE:

			// 是否开启下拉更新
			if (!isOffRefresh) {
				break; // 不开启下拉刷新直接break
			}

			// 判断轮播图如果不是完全显示,brak
//			if (!isShowFullPager()) {
//				break;
//			}

			// 正在刷新
			if (headState == HEAD_STATE_YES_REFRESH) {
				break;
			}

			if (downY == -1) { // 要是没有获得按下的坐标,就现在获得
				downY = ev.getY();
			}

			float moveY = ev.getY(); // 移動y坐标位置

			float dy = moveY - downY; // 获得移动坐标点

			if (dy > 0 && getFirstVisiblePosition() == 0) {
				float headY = -headHeight + dy;
				// 下拉刷新状态
				if (headY < 0 && headState != HEAD_STATE_DOWN_REFRESH) {
					headState = HEAD_STATE_DOWN_REFRESH; // 记住状态
					refresh(); // 刷新
				} else if (headY >= 0 && headState != HEAD_STATE_UP_REFRESH) {
					// 释放刷新状态
					headState = HEAD_STATE_UP_REFRESH;
					refresh(); // 刷新
				}
				rl.setPadding(0, (int) headY, 0, 0);
				return true;
			}

			break;
		case MotionEvent.ACTION_UP:
			downY = -1; // 重置按下坐标
			// 下拉刷新状态
			if (headState == HEAD_STATE_DOWN_REFRESH) {
				// 松开回到原位
				rl.setPadding(0, -headHeight, 0, 0);
			} else if (headState == HEAD_STATE_UP_REFRESH) {
				// 松开刷新
				headState = HEAD_STATE_YES_REFRESH; // 改变状态
				rl.setPadding(0, 0, 0, 0);
				refresh(); // 刷新界面
				// TODO 刷新数据回调
				if (null != listener) {
					listener.onRefreshDatas();
					setRefreshDate(); // 刷新时间
				}
			}
			break;
		default:
			break;
		}

		return super.onTouchEvent(ev);
	}

	/**
	 * 下拉刷新完成,头部归位
	 */
	public void isFinishRefresh() {

		if (isLoadingMor) {
			tail.setPadding(0, -tailHeight, 0, 0);
			isLoadingMor = false;
		} else {
			rl.setPadding(0, -headHeight, 0, 0); // 刷新完成回到原位
			des.setText("下拉刷新");
			pb.setVisibility(View.INVISIBLE);
			arrow.setVisibility(View.VISIBLE);
			headState = HEAD_STATE_DOWN_REFRESH;
		}
	}

	
	/**
	 * @author jiqian
	 * @创建时间 2015-7-9 下午6:28:19
	 * @描述 下拉刷新接口回调
	 * 
	 * @svn 提交者: $Author: super $
	 * @提交时间 $Date: 2015-07-12 21:09:37 +0800 (Sun, 12 Jul 2015) $
	 * @当前版本 $Rev: 11 $
	 * 
	 */
	public interface OnCallBackRefreshListener {
		/**
		 * 刷新数据
		 */
		void onRefreshDatas();

		/**
		 * 刷新成功请返回new Date(),刷新失败请返回null;
		 * 
		 * @return new Date();
		 */
		String onSetPreviousRefreshDate();

		/**
		 * 上拉加载数据
		 */
		void loadingMor();
	}

	/**
	 * 设置回调刷新时间保存
	 */
	private void setRefreshDate() {
		String date = listener.onSetPreviousRefreshDate();
		if (!TextUtils.isEmpty(date)) {
			cn.linjiqian.downuprefreshlistview_library.utils.SpUtils.saveString(getContext(), "refresh_date", date);
		}
	}

	/**
	 * 设置监听事件
	 * 
	 * @param listener
	 */
	public void setOnCallBackRefreshLinstener(OnCallBackRefreshListener listener) {
		this.listener = listener;
	}

	/**
	 * 刷新状态
	 */
	private void refresh() {
		// 设置上次刷新时间
		time.setText("上次刷新:"
				+ cn.linjiqian.downuprefreshlistview_library.utils.SpUtils.readString(getContext(),
						"refresh_date"));
		switch (headState) {
		// 下拉刷新
		case HEAD_STATE_DOWN_REFRESH:

			des.setText("下拉刷新");
			arrow.startAnimation(rotate_down); // 开始动画
			break;
		// 释放刷新
		case HEAD_STATE_UP_REFRESH:
			des.setText("松开刷新");
			arrow.startAnimation(rotate_up); // 开始动画
			break;
		// 正在刷新
		case HEAD_STATE_YES_REFRESH:
			des.setText("正在刷新");
			arrow.clearAnimation(); // 清除补间动画
			arrow.setVisibility(View.GONE);
			pb.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}

	/**
	 * 头部
	 */
	private void initHead() {

		head = (LinearLayout) View.inflate(getContext(),
				R.layout.view_downup_listview, null);
		System.out.println("head:=" + head);
		// 加载布局
		rl = (RelativeLayout) head.findViewById(R.id.rl_view_listview_layout);
		// 箭头
		arrow = (ImageView) head
				.findViewById(R.id.iv_view_downup_listview_arrow);
		// 进度条
		pb = (ProgressBar) head.findViewById(R.id.pb_view_downup_listview_pb);
		// 提示
		des = (TextView) head.findViewById(R.id.tv_view_downup_listview_title);

		// 时间
		time = (TextView) head.findViewById(R.id.tv_view_downup_listview_titme);
		// 设置上次刷新时间
		time.setText("上次刷新:"
				+ cn.linjiqian.downuprefreshlistview_library.utils.SpUtils.readString(getContext(),
						"refresh_date"));

		// 测量
		rl.measure(0, 0);
		// 获得测量后的参数
		headHeight = rl.getMeasuredHeight();
		// 利用padding隐藏头部
		rl.setPadding(0, -headHeight, 0, 0);
		addHeaderView(head); // 添加头部
	}

	/**
	 * 初始化动画
	 */
	private void initRotateAnimation() {
		rotate_up = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		rotate_up.setDuration(600);
		rotate_up.setFillAfter(true);

		rotate_down = new RotateAnimation(-180, -360,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rotate_down.setDuration(600);
		rotate_down.setFillAfter(true);
	}

	/**
	 * 尾部
	 */
	private void initTail() {

		tail = View.inflate(getContext(), R.layout.view_downup_listview_tail,
				null);
		// 测量
		tail.measure(0, 0);
		tailHeight = tail.getMeasuredHeight();
		// 隐藏
		tail.setPadding(0, -tailHeight, 0, 0);
		this.addFooterView(tail); // 添加尾部布局
	}

	/**
	 * 给用户自己设置是否要开启下拉刷新
	 * 
	 * @param yesOnNo
	 */
	public void setIsYesDownRefresh(boolean yesOnNo) {
		isOffRefresh = yesOnNo;
	}

	public void addLunBoTu(View view) {
		// 判断是否要启用下拉刷新
		if (isOffRefresh) {
			// 启用加载刷新头
			lunBoTuPager = view; // 轮播图
			head.addView(view); // 添加轮播图
		} else {
			// 不启用,走父类逻辑
			super.addHeaderView(view);
		}
	}
}
