package com.wenchen.android.earthquakeviewer;

/**
 * Created by WenChen on 2018/5/22.
 */

//使用快速排序的类

//排序方法全部设为私有的，用于无法调用
public class QuickSort
{
	private double[] list;

	public QuickSort(double[] list)
	{
		this.list = list;
	}

	//排序函数
	private void sort()
	{
		sort(0, list.length - 1);
	}

	private void sort(int first, int last)
	{
		if(first < last) //pivot: 主元
		{ //partition：划分
			int pivotIndex = partition(list, first, last);
			sort(first, pivotIndex - 1);
			sort(pivotIndex + 1, last);
		}
	}

	//划分函数，返回主元下标
	private int partition(double[] list, int first, int last)
	{
		double pivot = list[first]; //第一个数作为主元
		int low = first + 1;
		int high = last;

		while(high > low)
		{
			while(low <= high && list[low] <= pivot)
				low++;
			while(low <= high && list[high] > pivot)
				high--;

			if(high > low)
			{
				double temp = list[high];
				list[high] = list[low];
				list[low] = temp;
			}
		}

		if(pivot > list[high])
		{
			list[first] = list[high];
			list[high] = pivot;
			return high; //返回主元下标
		}
		else
			return first;
	}

	//获得double型数组
	public double[] getDoubleArray()
	{
		sort();
		return list;
	}

	//获得int型数组
	public int[] getIntegerArray()
	{
		sort();

		//将double型数组转为int型
		int[] temp = new int[list.length];
		for(int i = 0; i < list.length; i++)
		{
			temp[i] = (int)list[i];
		}

		return temp;
	}
}
