package woowacourse.movie.presentation.activities.ticketing

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import woowacourse.movie.R
import woowacourse.movie.domain.model.movie.DomainMovieDate
import woowacourse.movie.presentation.activities.main.fragments.theaterPicker.TheaterPickerDialog.Companion.MOVIE_KEY
import woowacourse.movie.presentation.activities.main.fragments.theaterPicker.TheaterPickerDialog.Companion.THEATER_KEY
import woowacourse.movie.presentation.activities.ticketing.seatpicker.SeatPickerActivity
import woowacourse.movie.presentation.extensions.getParcelableCompat
import woowacourse.movie.presentation.extensions.showBackButton
import woowacourse.movie.presentation.mapper.toPresentation
import woowacourse.movie.presentation.model.MovieDate
import woowacourse.movie.presentation.model.MovieTime
import woowacourse.movie.presentation.model.Ticket
import woowacourse.movie.presentation.model.item.Movie
import woowacourse.movie.presentation.model.item.Theater

class TicketingActivity : AppCompatActivity(), View.OnClickListener, TicketingContract.View {
    override lateinit var presenter: TicketingPresenter

    private var selectedDate: MovieDate? = null
    private var selectedTime: MovieTime? = null

    private val movieDates: List<MovieDate> by lazy {
        intent.getParcelableCompat<Movie>(MOVIE_KEY)?.run {
            DomainMovieDate.releaseDates(from = startDate, to = endDate).map { it.toPresentation() }
        } ?: emptyList()
    }

    private val movieTimes: List<MovieTime> by lazy {
        intent.getParcelableCompat<Theater>(THEATER_KEY)?.run { screenTimes } ?: emptyList()
    }

    private val movieDateAdapter: ArrayAdapter<String> by lazy {
        ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            movieDates.map { getString(R.string.book_date, it.year, it.month, it.day) },
        )
    }
    private val movieTimeAdapter: ArrayAdapter<String> by lazy {
        ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableListOf())
    }
    private val movieTimeSpinner: Spinner by lazy { findViewById(R.id.movie_time_spinner) }
    private val movieDateSpinner: Spinner by lazy { findViewById(R.id.movie_date_spinner) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticketing)

        initView()
        restoreState(savedInstanceState)
        showBackButton()

        initSpinnerConfig()
        initViewClickListener()
    }

    private fun initView() {
        val movie = intent.getParcelableCompat<Movie>(MOVIE_KEY)!!
        presenter = TicketingPresenter(this, movie)
        presenter.showMovieIntroduce()
        presenter.updateMovieTimes()
    }

    private fun initViewClickListener() {
        findViewById<Button>(R.id.minus_btn).setOnClickListener(this@TicketingActivity)
        findViewById<Button>(R.id.plus_btn).setOnClickListener(this@TicketingActivity)
        findViewById<Button>(R.id.ticketing_btn).setOnClickListener(this@TicketingActivity)
    }

    private fun initSpinnerConfig() {
        initSpinnerAdapter()
        initSpinnerListener()
    }

    private fun initSpinnerAdapter() {
        movieDateSpinner.adapter = movieDateAdapter.also { it.setNotifyOnChange(true) }
        movieTimeSpinner.adapter = movieTimeAdapter.also { it.setNotifyOnChange(true) }
    }

    private fun initSpinnerListener() {
        initMovieTimeSpinnerListener()
        initMovieDateSpinnerListener()
    }

    private fun initMovieTimeSpinnerListener() {
        movieTimeSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                itemView: View?,
                pos: Int,
                id: Long,
            ) {
                selectedTime = movieTimes[pos]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun initMovieDateSpinnerListener() {
        movieDateSpinner.onItemSelectedListener =
            object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    itemView: View?,
                    pos: Int,
                    id: Long,
                ) {
                    selectedDate = movieDates[pos]
                    selectedTime = movieTimes.first()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
    }

    override fun setMovieTimes() {
        val times = movieTimes.map { movieTime ->
            getString(R.string.book_time, movieTime.hour, movieTime.min)
        }
        movieTimeAdapter.addAll(times)
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        savedInstanceState?.run {
            selectedDate = getParcelableCompat(SELECTED_DATE_STATE_KEY)!!
            selectedTime = getParcelableCompat(SELECTED_TIME_STATE_KEY)!!
            presenter.movie = getParcelableCompat(TICKET_COUNT_STATE_KEY)!!
        }
        presenter.updateMovieTimes()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        presenter.setTicketCount()
        movieDateSpinner.setSelection(movieDates.indexOf(selectedDate))
        movieTimeSpinner.setSelection(movieTimes.indexOf(selectedTime))
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.minus_btn -> {
                presenter.subTicketCount()
            }

            R.id.plus_btn -> {
                presenter.addTicketCount()
            }

            R.id.ticketing_btn -> {
                presenter.moveToSeatPickerActivity(selectedDate, selectedTime)
            }
        }
    }

    override fun showToast(message: String) {
        this.showToast(message)
    }

    override fun startSeatPickerActivity() {
        startActivity(
            Intent(this, SeatPickerActivity::class.java)
                .putExtra(MOVIE_KEY, intent.getParcelableCompat<Movie>(MOVIE_KEY))
                .putExtra(TICKET_KEY, presenter.getMovieTicket())
                .putExtra(MOVIE_DATE_KEY, selectedDate)
                .putExtra(MOVIE_TIME_KEY, selectedTime),
        )
        finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(TICKET_COUNT_STATE_KEY, presenter.getMovieTicket())
        outState.putParcelable(SELECTED_DATE_STATE_KEY, selectedDate)
        outState.putParcelable(SELECTED_TIME_STATE_KEY, selectedTime)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setTicketCountText(movieTicket: Ticket) {
        findViewById<TextView>(R.id.ticket_count_tv).text = movieTicket.count.toString()
    }

    override fun setMovieData(movie: Movie) {
        with(movie) {
            findViewById<ImageView>(R.id.poster_iv).setImageResource(thumbnail)
            findViewById<TextView>(R.id.title_tv).text = title
            findViewById<TextView>(R.id.date_tv).text = getString(
                R.string.movie_release_date,
                startDate.formattedDate,
                endDate.formattedDate,
            )
            findViewById<TextView>(R.id.running_time_tv).text =
                getString(R.string.movie_running_time, runningTime)
            findViewById<TextView>(R.id.introduce_tv).text = introduce
        }
    }

    companion object {
        internal const val TICKET_KEY = "ticketCount"
        internal const val MOVIE_DATE_KEY = "movieDate"
        internal const val MOVIE_TIME_KEY = "movieTime"

        internal const val SELECTED_DATE_STATE_KEY = "selectedDate"
        internal const val SELECTED_TIME_STATE_KEY = "selectedTime"
        internal const val TICKET_COUNT_STATE_KEY = "ticketCountState"
    }
}
