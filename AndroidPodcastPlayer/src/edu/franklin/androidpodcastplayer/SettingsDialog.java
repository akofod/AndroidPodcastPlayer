package edu.franklin.androidpodcastplayer;

import java.util.HashMap;
import java.util.Map;

import edu.franklin.androidpodcastplayer.models.Subscription;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

public class SettingsDialog extends LinearLayout 
{
	private CheckBox autoDownload = null;
	private RadioGroup episodeGroup = null;
	private RadioGroup frequencyGroup = null;
	private Subscription subscription = null;
	
	private static Map<Integer, Long> idToSettingMap = new HashMap<Integer, Long>();
	private static Map<Long, Integer> settingToIdMap = new HashMap<Long, Integer>();
	
	//populate the map values
	static
	{
		//map control to subscription value
		idToSettingMap.put(R.id.subscriptionRadioOne, Subscription.ONE);
		idToSettingMap.put(R.id.subscriptionRadioThree, Subscription.THREE);
		idToSettingMap.put(R.id.subscriptionRadioFive, Subscription.FIVE);
		idToSettingMap.put(R.id.subscriptionRadioAll, Subscription.ALL);
		idToSettingMap.put(R.id.subscriptionRadioDaily, Subscription.DAILY);
		idToSettingMap.put(R.id.subscriptionRadioWeekly, Subscription.WEEKLY);
		idToSettingMap.put(R.id.subscriptionRadioManually, Subscription.MANUAL);
		//map subscription value to control
		settingToIdMap.put(Subscription.ONE, R.id.subscriptionRadioOne);
		settingToIdMap.put(Subscription.THREE, R.id.subscriptionRadioThree);
		settingToIdMap.put(Subscription.FIVE, R.id.subscriptionRadioFive);
		settingToIdMap.put(Subscription.ALL, R.id.subscriptionRadioAll);
		settingToIdMap.put(Subscription.DAILY, R.id.subscriptionRadioDaily);
		settingToIdMap.put(Subscription.WEEKLY, R.id.subscriptionRadioWeekly);
		settingToIdMap.put(Subscription.MANUAL, R.id.subscriptionRadioManually);
	}
	
	public SettingsDialog(Context context) 
	{
		super(context);
		//we laid out the view using the visual builder and an xml file, inflate it here.
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.settings_dialog, this);
		//get the controls we care about
		autoDownload = (CheckBox)findViewById(R.id.subscriptionAutoDownloadCheckbox);
		episodeGroup = (RadioGroup)findViewById(R.id.subscriptionEpisodeGroup);
		frequencyGroup = (RadioGroup)findViewById(R.id.subscriptionFrequencyGroup);
	}
	
	public void setSubscription(Subscription subscription)
	{
		//we clone the subscription so we can support a cancel operation
		this.subscription = subscription.copy();
		autoDownload.setChecked(subscription.isAutoDownload());
		//set the selected episode
		episodeGroup.check(settingToIdMap.get(subscription.getEpisodes()));
		//set the selected frequency
		frequencyGroup.check(settingToIdMap.get(subscription.getFrequency()));
	}
	
	public Subscription getSubscription()
	{
		subscription.setAutoDownload(autoDownload.isChecked());
		subscription.setEpisodes(idToSettingMap.get(episodeGroup.getCheckedRadioButtonId()));
		subscription.setFrequency(idToSettingMap.get(frequencyGroup.getCheckedRadioButtonId()));
		return subscription;
	}
}
